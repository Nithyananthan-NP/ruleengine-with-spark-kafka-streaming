ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.11"

lazy val root = (project in file("."))
  .settings(
    name := "InsuranceRuleEngine"
  )
resolvers ++= Seq(
  "confluent" at "https://packages.confluent.io/maven/"

)
name := "Insurance Streaming"
version := "1.0"
scalaVersion := "2.12.11"
val sparkVersion = "3.3.1"
val confluentVersion = "5.3.0"
resolvers += Resolver.bintrayIvyRepo("com.eed3si9n", "sbt-plugins")

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.3.1"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.3.1"
libraryDependencies += "org.kie" % "kie-ci" % "7.52.0.Final"
libraryDependencies += "org.kie.server" % "kie-server-client" % "7.52.0.Final"
libraryDependencies += "org.optaplanner" % "optaplanner-core" % "7.52.0.Final"
libraryDependencies += "org.optaplanner" % "optaplanner-persistence-jaxb" % "7.52.0.Final"
libraryDependencies += "com.thoughtworks.xstream" % "xstream" % "1.4.16"
libraryDependencies += "com.google.code.gson" % "gson" % "2.8.2"
//libraryDependencies += "org.mongodb.spark" % "mongo-spark-connector_2.12" % "3.0.1"
libraryDependencies += "org.mongodb.spark" %% "mongo-spark-connector" % "10.1.0"

libraryDependencies +="org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion
libraryDependencies +="org.apache.spark" %% "spark-avro" % sparkVersion
 libraryDependencies += "io.confluent" % "kafka-schema-registry-client" % "6.0.1"


libraryDependencies +=
  "org.scalatest" %% "scalatest" % "3.2.11" % Test



// libraryDependencies += "org.uberfire" % "uberfire-api" % "7.73.0.Final"
//libraryDependencies += "com.thoughtworks.xstream" % "xstream" % "1.4.16.redhat-00001"
//libraryDependencies += "org.codehaus.plexus" % "plexus-container-default" % "2.1.1"

