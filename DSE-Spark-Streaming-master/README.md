#Introduction

In this set of exercises we will be exploring Spark Streaming to acquire data from streaming data sources and put those into DSE/C*. Additionally, different streaming sources will be explored including local streams and Apache Kafka. Finally, we will be working with Spark Streaming specific functionality to show possible ways to take advantage of windowing and time slices.

#Prerequisites
These exercises were all built using DSE 4.6. At a minimum you should be using that version of DSE. For this series of exercises we will be running all of the examples against a single node cluster. The exercises were built using OS X with DSE loaded locally. A local install on a Linux environment should also work with on ly minor modifications. It is also possible to run these exercises on a remote cluster with minor changes to the code examples.

All interactions within these exercises will use Scala. Some familiarity with Scala will be very beneficial but is not an absolute requirement. All the exercises could potentially be completed using the DSE Python Spark integration. That effort is left as an exercise to the reader.

You will also need to have GitHub and "git" installed on the local machine. More info can be found here: [GitHub Site](https://github.com/)

Scala Build Tool (sbt) will also be required for this exercise. If you do not have this already installed you can locate it here: [sbt site](http://www.scala-sbt.org/)

##1. Spark Streaming from a local stream

The goal of this exercise is to create a very minimal Spark Streaming example. The example is the obligatory "Big Data" word count example with a streaming twist. You will create a Scala/Spark program that use Spark Streaming to pull data from a local TextStream. It will then split the lines of the stream into individual words and save those into a DSE/C* counter table. This will provide a total occurrence view of all the words coming across the stream. The program then used the windowing functions in Spark Streaming to output the occurrences of words that have happened in the last two minutes.

In this exercise will perform the following activities:

  * Verify local DSE version. Start DSE/cassandra with Spark enabled.
  * Clone this GitHub repository to your local machine
  * Validate that sbt is installed and available
  * Locate/Review/Modify the Scala code for this example
  * Prepare the command Windows for the example
  * Execute/Interact with the example.

Please proceed to the file [LoadFromLocalStream.md](./LoadFromLocalStream.md)

##2. Install and Prepare Apache Kafka

In this section of the exercise we will install Apache Kafka on the local machine and prepare it for use in the next exercise.

In this exercise you will perform the following activities:

  * Locate and download Apache Kafka
  * Install Apache Kafka
  * Start ZooKeeper and Kafka
  * Prepare a message topic for use.

Please proceed to the file [InstallPrepareApacheKafka.md](./InstallPrepareApacheKafka.md)

##3. Spark Streaming from Apache Kafka

******** UNDER DEVELOPMENT ************

