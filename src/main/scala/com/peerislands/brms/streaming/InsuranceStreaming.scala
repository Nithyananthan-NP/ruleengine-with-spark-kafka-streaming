package com.peerislands.brms.streaming

import com.peerislands.brms.util.Constants
import com.peerislands_space.insuranceprocess.Insurance
import org.apache.spark.SparkConf
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{DataTypes, StructType}
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.kie.api.KieServices
import org.kie.api.command.Command
import org.kie.api.runtime.{KieContainer, KieSession}
import org.kie.server.api.marshalling.MarshallingFormat
import org.kie.server.client.KieServicesFactory

import java.io.IOException
import java.util
import java.util.Properties
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}

/** A InsuranceStreaming is the spark structured streaming  class which runs continiously.
 *  Source Clonfluent kafka
 *  Sink Mongodb Atlas
 */
object InsuranceStreaming {

  def main(args: Array[String]) {
    val conf = new SparkConf
    val spark = SparkSession.builder.config(conf)
      .appName(Constants.AP_NAME)
      .master(Constants.SPARK_MASTER)
      .config(Constants.MONGODB_CONFIG_VALUE, Constants.MONGODB_CONFIG_VALUE)
      .getOrCreate()

    import spark.implicits._
    val configFileName = Constants.CONFIG_FILE_NAME
    //Build Confluent kafka properties
    val props = Constants.buildProperties(configFileName)

    //Populate rules from Redhat Central repository
    val rules = {
      loadRules(props).getKieBase.newKieSession()
    }


    //Define structure which is inline with rule engine
    val structIns = new StructType()
      .add("name", DataTypes.StringType)
      .add("address", DataTypes.StringType)
      .add("insuranceId", DataTypes.LongType)
      .add("age", DataTypes.LongType)
      .add("hasIncident", DataTypes.BooleanType)
      .add("premium", DataTypes.DoubleType)

    //Read Stream from confluent kafka topic
    val inputJsonDf = spark.readStream
      .format("kafka")
      .option(props.getProperty("kafka.server.key"), props.getProperty("kafka.server.value"))
      .option(props.getProperty("kafka.server.protocol.key"), props.getProperty("kafka.server.protocol.value"))
      .option(props.getProperty("kafka.server.sasl.key"), props.getProperty("kafka.server.sasl.value"))
      .option(props.getProperty("kafka.sasl.jaas.key"), props.getProperty("kafka.sasl.jaas.value"))
      .option(props.getProperty("kafka.subscribe.key"), props.getProperty("kafka.subscribe.value")) // going to replay from the beginning each time
      .option(props.getProperty("kafka.startingOffsets.key"), props.getProperty("kafka.startingOffsets.value"))
      .load()
      .selectExpr("CAST(value AS STRING)")
      .select(from_json($"value", structIns).as("ins"))

    //Select the relevant information
    val selectDf = inputJsonDf.selectExpr(Constants.INS_NAME, Constants.INS_AGE, Constants.INS_HAS_INCIDENT, Constants.INS_ADDRESS, Constants.INS_INSURANCE_ID, Constants.INS_PREMIUM).as[Insurance]

    //Broadcast  the rules to all executors which are part of the cluster nodes
    var broadcastStates = spark.sparkContext.broadcast(rules)

    //Separate thread to monitor and update the broadcast rules
    runScheduledThreadToUpdateBroadcastVariable

    def runScheduledThreadToUpdateBroadcastVariable(): Unit = {
      val updateTask = new Runnable {
        def run() = {
          broadcastStates.unpersist(blocking = false)
          broadcastStates = spark.sparkContext.broadcast(loadRules(props).getKieBase.newKieSession())
        }
      }

      val executor = new ScheduledThreadPoolExecutor(1)
      executor.scheduleAtFixedRate(updateTask, 1, 5, TimeUnit.SECONDS)
    }

    import org.apache.spark.sql.Encoders
    //write to mongodb collection where the rules values are applied based on the inputs and condition matching
    writeStream("premium>0")
    //writeStream("premium<=0")

    def writeStream(condition: String) = {
      val result = selectDf.map(a => applyRules(broadcastStates.value, a), Encoders.bean(classOf[Insurance]))
      result.filter(condition).writeStream
        .outputMode(SaveMode.Append.name())
        .format(Constants.FORMAT)
        .option(props.getProperty("checkpointLocation.key"), props.getProperty("checkpointLocation.value"))
        .option(props.getProperty("spark.mongodb.connection.uri.key"), props.getProperty("spark.mongodb.connection.uri.value"))
        .option(props.getProperty("spark.mongodb.database.key"), props.getProperty("spark.mongodb.database.value"))
        .option(props.getProperty("spark.mongodb.collection.key"), props.getProperty("spark.mongodb.collection.value"))
        .start()
    }

    spark.streams.awaitAnyTermination()
  }


  /** Load rules based from redhat central repository .
   *
   * @param props  Properties
   * @return KieContainer which contains all the rule base
   * @throws IOException
   */
  @throws[IOException]
  def loadRules(props: Properties): KieContainer = {
    val allClasses = new util.HashSet[Class[_]]
    val serverUrl = props.getProperty("serverUrl")
    val username = props.getProperty("username")
    val password = props.getProperty("password")
    val config = KieServicesFactory.newRestConfiguration(serverUrl, username, password)
    config.setMarshallingFormat(MarshallingFormat.JAXB)
    config.addExtraClasses(allClasses)
    val kieServicesClient = KieServicesFactory.newKieServicesClient(config)
    val kieServices = KieServices.Factory.get
    val kContainer = kieServices.newKieContainer(kieServicesClient.getReleaseId(Constants.CONTANIER_NAME).getResult)
    kContainer
  }

  /** Applies rules based from streaming input data .
   *
   * @param kieSession  KieSession
   * @return Insurance which contains the updated rule values based on the input dataset
   *
   */
  def applyRules(kieSession: KieSession, a: Insurance): Insurance = {
    val kieCommands = KieServices.Factory.get.getCommands
    val commandList = new util.ArrayList[Command[_]]
    commandList.add(kieCommands.newInsert(a))
    // Fire all rules:
    commandList.add(kieCommands.newFireAllRules())
    val batch = kieCommands.newBatchExecution(commandList)
    kieSession.execute(batch)
    a;
  }
}
