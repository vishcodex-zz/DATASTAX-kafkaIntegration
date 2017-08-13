#Overview

In this section of the exercise will simulate a Time Series data flow into a Kafka Stream. A Spark streaming job will then read the Kafka topic and insert the values into a Cassandra data model. Finally, the Spark Streaming job will use the windowing features of Spark  Streaming to show the by sensor totals for the latest five seconds in the output. As you can tell, this is just the beginning of a much

In this exercise you will perform the following activities:

1. Prepare the technical environment for the demo
2. Locate/Review/Modify the Scala code for this example
3. Prepare the command windows for the example
4. Execute/interact with the example

#Requirements

For this part of the Exercise you will need the following
  * DSE 4.6+ installed locally on either an OS X or Linux environment
  * git installed locally and access to GitHub: [GitHub Site](https://github.com/)
  * sbt installed locally: [sbt site](http://www.scala-sbt.org/)
  * Access to an Apache Kafka install. Follow these instructions for a local install: [InstallPrepareApacheKafka.md](./InstallPrepareApacheKafka.md)


##1 Prepeare the technical environment for the demo

Prior to running working through this exercise should have worked through these proceeeding exercise:

  * [LoadFromLocalStream.md](./LoadFromLocalStream.md)
  * [InstallPrepareApacheKafka.md](./InstallPrepareApacheKafka.md)

In addition to the successful completion of those two exercises you will need to make some configuration modifications to your DSE install if you are running DSE 4.6.0. (Note: these changes may not be necessary if you are running versions of DSE > 4.6.0 as the issues should be addressed in future releases.

The details of those changes are outlined below:

1. On each machine in the DSE cluster navigate to the root directory of the DSE install. This will vary depending on the install methondology used. Once there,

2. remove/rename "resources/spark/lib/libthrift-0.8.0.jar" - We want to make sure it is not on the path.

3. comment out this line below in "resources/spark/bin/spark-class"

        #SPARK_JAVA_OPTS+=" -Djava.system.class.loader=$DSE_CLIENT_CLASSLOADER"

4. restart the DSE instanace

##2 Locate/Review/Modify the Scala code for this example

If you have not done so already, you will need to get the project code from GitHub.

Navigate to a directory that you would like to use for this project. From the command line in that directory issue the following command

                git clone https://github.com/CaryBourgeois/DSE-Spark-Streaming.git

This project was created with the Community Edition of IntelliJ IDEA. The simplest way to review and modify the scala code is to open the project with this IDE. Alternatively, you can use any text editor to view/edit the file as the build and execute process will work from the command line via sbt.

From the directory where you cloned the GitHub project, navigate to the `/src/main/scala` directory. Locate and open the file `LoadFromKafkaStream.scala`.

The code is well documented so you should review it and understand the general flow. Keep in mind, there are two executable objects in the source file.

  * `LoadFromKafkaStream` - is the scala object for acquiring data from the Kafka stream and then writing that data to DSE/Cassandra and using the Spark Streaming Windowing functions.
  * `KakfkaStreamProducer` - is the scala object that simultes time series sensor data and places it on the Kafka stream.

As you review this code pay close attention to some of the defined constants that control the configuration of DSE and Kafka. Please adjust those to fit your configuration. If you are running everything local you should not need to make any changes.

Save your changes to the code if you have made any.

From the root of the project execute the `sbt assembly` command. This should run cleanly and complete with a `[success]` message.

##3 Prepare the command windows for the example

To run this demo you will need to have a least 2 command line windows open. In OS X this can be accomplished by using multiple tabs.  This approach assumes that you have DSE and Kakfa up and running as specified in the earlier exercise.

Set the tabs up in the following way:

  1. All of these command assume that you are art the root directory of the project.

  2. The first window will be dedicated to the Kafka Stream producer. Start the program using the command `sbt run`. You should see a prompt that looks something like the one below. Select the `KafkaStreamProducer` option. NOTE: the order and number of the options can vary so select carefully.

        Multiple main classes detected, select one to run:

         [1] LoadFromKafkaStream
         [2] KafkaStreamProducer
         [3] LoadFromLocalStream

        Enter number:
    Once started, the output from the program should look somehting like this:

        [info] Running KafkaStreamProducer

        KeyedMessage(stream_ts,null,1;2015-02-19 14:58:01.536;81.8174288748221)
        KeyedMessage(stream_ts,null,2;2015-02-19 14:58:01.538;65.34671514986411)
        KeyedMessage(stream_ts,null,3;2015-02-19 14:58:01.538;64.08766390842354)
        KeyedMessage(stream_ts,null,4;2015-02-19 14:58:01.538;84.17078234886762)
        KeyedMessage(stream_ts,null,5;2015-02-19 14:58:01.538;53.5543785557487)
        KeyedMessage(stream_ts,null,1;2015-02-19 14:58:02.694;70.26547217763967)
        KeyedMessage(stream_ts,null,2;2015-02-19 14:58:02.694;78.18345150242011)
        KeyedMessage(stream_ts,null,3;2015-02-19 14:58:02.694;62.91234187164536)
        KeyedMessage(stream_ts,null,4;2015-02-19 14:58:02.694;63.23790579404052)
        KeyedMessage(stream_ts,null,5;2015-02-19 14:58:02.694;93.7043230505889)
        KeyedMessage(stream_ts,null,1;2015-02-19 14:58:03.699;67.51464417482589)


  3. The second window will be dedicated to the Spark app that reads from the Kafka stream. Start the program using the same `sbt run` command but in this window select the `LoadFromKafkaStream` option. It will take a few seconds to start up but you should see out put that looks similar to the output below:

        -------------------------------------------
        Time: 1424379708000 ms
        -------------------------------------------
        (4,368.90410213457244)
        (2,392.9379247424991)
        (5,350.71617258013714)
        (3,333.3391568761312)
        (1,365.19043767773275)

        -------------------------------------------
        Time: 1424379709000 ms
        -------------------------------------------
        (4,388.72273274520796)
        (2,387.1717357665636)
        (5,345.8734775317224)
        (3,321.0647645796324)
        (1,368.0765533386294)

As a final chaeck you may want to switch over to the CQL Shell and query the data table where the stream was written. The query to use for that would look something like this:

        SELECT * FROM spark_cass.stream_ts LIMIT 10;







