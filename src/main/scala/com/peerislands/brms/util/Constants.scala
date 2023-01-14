package com.peerislands.brms.util

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.spark.sql.types.{DataTypes, StructType}

import java.io.FileReader
import java.util.Properties

object Constants {
  val AP_NAME = "InsuranceRuleEngine-StreamProcessing"
  val SPARK_MASTER = "local[*]"
  val DATABASE = "sample_db"
  val COLLECTION_WITH_RULES = "AppliedWithRulesTest"
  val COLLECTION_WITH_OUT_RULES = "AppliedWithOutRulesTest"
  val NAME = "Distributed Rule Engine"
  val MONGODB_SOURCE = "com.mongodb.spark.sql.DefaultSource"
  val MONGODB_CONFIG_KEY = "spark.mongodb.output.uri"
  val MONGODB_CONFIG_VALUE = "mongodb+srv://test:test@cluster0.37iyayq.mongodb.net/?retryWrites=true&w=majority"
  val CONFIG_FILE_NAME = "/Users/nithyananthan/Downloads/InsuranceRuleEngine/ccloud-config.properties"
  val INS_NAME = "ins.name"
  val INS_AGE = "ins.age"
  val INS_HAS_INCIDENT = "ins.hasIncident"
  val INS_ADDRESS = "ins.address"
  val INS_INSURANCE_ID = "ins.insuranceId"
  val INS_PREMIUM = "ins.premium"
  val FORMAT = "mongodb"
  val CONTANIER_NAME="InsuranceProcess_1.0.0-SNAPSHOT"

  val STRUCT_INS = new StructType()
    .add("name", DataTypes.StringType)
    .add("age", DataTypes.LongType)
    .add("hasIncident", DataTypes.BooleanType)
    .add("address", DataTypes.StringType)
    .add("insuranceId", DataTypes.LongType)
    .add("premium", DataTypes.DoubleType)

  // "spark.mongodb.output.uri",
  def buildProperties(configFileName: String): Properties = {
    val properties: Properties = new Properties
    properties.load(new FileReader(configFileName))
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.connect.json.JsonSerializer")
    properties
  }

}
