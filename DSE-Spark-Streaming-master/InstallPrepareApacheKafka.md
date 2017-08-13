#Overview
In this section of the exercise we will install Apache Kafka on the local machine and prepare it for use in the next exercise.

In this exercise you will perform the following activities:

1. Locate and download Apache Kafka
2. Install Apache Kafka
3. Start ZooKeeper and Kafka
4. Prepare a message topic for use.

##1. Locate and download Apache Kafka

Kafka can be located at this URL: [http://kafka.apache.org/downloads.html](http://kafka.apache.org/downloads.html)

You will want to download and install the binary version for Scala 2.10.

##2. Install Apache Kafka

Once downloaded you will need to extract the file. It will create a folder/directory. Move this to a location of your choice.

##3. Start ZooKeeper and Kafka

Start local copy of zookeeper

  * `<kafka home dir>/bin/zookeeper-server-start.sh config/zookeeper.properties`

Start local copy of Kafka

  * `<kafka home dir>/bin/kafka-server-start.sh config/server.properties`

##4. Prepare a message topic for use.

Create the topic we will use for the demo

  * `<kafka home dir>/bin/kafka-topics.sh --zookeeper localhost:2181 --create --replication-factor 1 --partitions 1 --topic stream_ts`

Validate the topic was created. If this is a new kafka instance should just show test in list.

  * `<kafka home dir>/bin/kafka-topics.sh --zookeeper localhost:2181 --list`
