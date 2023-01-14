# Redhat Decision Manager Integration with Apache Spark-kafka-streaming pipelines

## Areas of focus
* De-couple complex Rule Process from business applications

* Centralized Rule System which can be used by multiple applications

* Configuration driven development of Rule creation and Rule Management without/less technical skill set.

* Increase performance as decision manager  uses the Rete algorithm to optimize the logical flow of rules, also by integrating the Rule engine with Spark 
processer will make it as an distributed rule engine 

## Red Hat Decision Manager-Rule Management Tool
<img width="746" alt="image" src="https://user-images.githubusercontent.com/18047704/212457850-94f2e9f7-2d26-4e37-8a61-299e7535a0e0.png">

## Architectural Design
<img width="951" alt="image" src="https://user-images.githubusercontent.com/18047704/212457911-60de2b5c-5dd6-49b8-a9e2-13e259d85b1f.png">

## Technology Stack

* Red Hat Decision Manager – Rule Management Tool-Configuration driven rule management without 
* Spark SQL- Scheduled Batch processing with distribued Rule process demo
* Spark Structured Streaming- Real time processing with distribued Rule process demo
* Confluent Kafka- Source of input for Streaming processing
* Atlas MongoDB – Sink for processed data
* REST API- .Net 
* Angular-  Data vizualization

## Step by Step apparoch

* Create Rule using Redhat Decision Manager Tool (Configuration driven development without programming)
* Publish the Rule to the Redhat central repository
* Build a client application using :
  * Kafka simulator publish the data to Confluent cloud
  
  * Spark Structured Streaming connects to the Redhat central repository reads the rules from the KIE Server Container and broadcasts the rules to all the     executors in the worker nodes
  
  * As a next configure the spark structured streaming source to the confluent kafka topic
  
  * Apply rules which are broadcasted to the incoming dataset 
  
  * Classify the Result Set in to two different based on the output of the applied rules
  
  * Store the results in to mongodb
    
