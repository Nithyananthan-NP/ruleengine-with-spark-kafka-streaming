import com.peerislands.brms.util.Constants
import com.peerislands_space.insuranceprocess.Insurance
import org.apache.spark.SparkConf
import org.apache.spark.sql.execution.streaming.{LongOffset, MemoryStream}
import org.apache.spark.sql.functions.from_json
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.{Files, Paths}

class StreamingInsuranceSpec extends AnyFunSuite {

  test("Should load insurance data to in memory streaming") {

    val conf = new SparkConf
    val spark = SparkSession.builder.config(conf)
      .appName(Constants.AP_NAME)
      .master(Constants.SPARK_MASTER)
      .config(Constants.MONGODB_CONFIG_VALUE, Constants.MONGODB_CONFIG_VALUE)
      .getOrCreate()



    implicit val sqlCtx: SQLContext = spark.sqlContext
    import spark.implicits._
    val events = MemoryStream[Insurance]
    val sessions = events.toDS

    assert(sessions.isStreaming, "sessions must be a streaming Dataset")


    val rsvpStruct = Constants.STRUCT_INS
    val transformedSessions = sessions.select(from_json($"name", rsvpStruct).as("ins"))

    val streamingQuery = transformedSessions
      .writeStream
      .format("memory")
      .queryName("ins")
      .outputMode("append")
      .start


    val batch =  Files.newBufferedReader(Paths.get("/Users/nithyananthan/Downloads/InsuranceRuleEngine/src/main/resources/ins.json"))
    import com.google.gson.Gson
    val reviews: Array[Insurance] = new Gson().fromJson(batch, classOf[Array[Insurance]])

    val currentOffset = events.addData(reviews)

    streamingQuery.processAllAvailable()
    events.commit(currentOffset.asInstanceOf[LongOffset])

    val rsvpEventName = spark.sql("select count(*) from ins")
      .collect()
      .map(_.getAs[Long](0))
      .head
    assert(rsvpEventName == 250)

  }

}
