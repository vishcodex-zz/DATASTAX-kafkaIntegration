/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Below are the libraries required for this project.
 * In this example all of the dependencies are included with the DSE 4.6 distribution.
 * We need to account for that fact in the build.sbt file in order to make sure we don't introduce
 * library collisions upon deployment to the runtime.
 */

import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}

object LoadFromLocalStream {
  /*
   * This is the entry point for the application
   */
  def main(args: Array[String]) {
    /*
     * This next line sets the logger level. If you are having trouble getting this program to work you can change the
     * value from Level.ERROR to LEVEL.WARN or more verbose yet, LEVEL.INFO
     */
    Logger.getRootLogger.setLevel(Level.ERROR)

    /*
     * The first step in this process is to set up the context for configuration for the Spark instance being used.
     * For this example the configuration reflects running DSE/Spark on the local system. In a production system you
     * would want to modify the host and Master to reflect your installation.
     */
    val sparkConf = new SparkConf().set("spark.cassandra.connection.host", "127.0.0.1")

      /*
       * The next three .set lines for the SparkConf are important for the Spark Streaming
       * Specifically you have to ensure that you have more than 1 core to run a Spark Streaming App
       * This can either be done at the system level via the config files or as below at the app level
       * The memory number is somewhat arbitrary in this case but 512M is enough here
       * The cleaner parameter is used to make sure the Spark checkpoint logs don't grow excessively , I Think :-)
       */
      .set("spark.cores.max", "2")
      .set("spark.executor.memory", "512M")
      .set("spark.cleaner.ttl", "3600") // This setting is specific to Spark Streaming. It set a flush time for old items.
      .setJars(Array("target/scala-2.10/DSE-Spark-Streaming-assembly-1.0.jar"))
      .setMaster("spark://127.0.0.1:7077")
      .setAppName("DSE Spark Streaming-Load from Local Stream")
      /*
         * The next two lines that are commented can be used to trace the execution of the
         * job using the sparkUI. On a local system, where this code would work, the URL for the
         * spark UI would be http://127.0.0.1:7080.
         * Before un-commenting these lines, make sure the spark.eventLog.dir exist and is
         * accessible by the process running spark.
        */
      //.set("spark.eventLog.enabled", "true")
      //.set("spark.eventLog.dir", "/Users/carybourgeois/Datastax/log/spark-events")

    /*
     * Create a connector object so that we can talk to DSE directly
     */
    val connector = CassandraConnector(sparkConf)

    // Get a session object from the connector. This will be the object used to talk directly with DSE
    connector.withSessionDo(session => {
      // Make sure that the keyspace we want to use exists and if not create it.
      // Change the topology an replication factor to suit your cluster.
      session.execute(s"CREATE KEYSPACE IF NOT EXISTS spark_cass WITH REPLICATION = { 'class':'SimpleStrategy', 'replication_factor':1}")

      // Below the data table is DROPped and re-CREATEd to ensure that we are dealing with new data.
      session.execute(s"DROP TABLE IF EXISTS spark_cass.stream_wc")
      session.execute(s"CREATE TABLE IF NOT EXISTS spark_cass.stream_wc (word text, count counter, PRIMARY KEY(word))")
    })

    // create a new SparkStreamingContext. Notice the second argument is the frequency with which the stream is polled.
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    ssc.checkpoint("checkpoint")

    // Create an RFF from a Text Stream. In this example we will have started the stream from the command line with the command
    // $ nc -lk 9999
    // This works on OS X and Linux to initiate a stream on the localhost/127.0.0.1. Any thing type now will be sent to
    // the 127.0.0.1:9999 address as a text stream.
    val lines = ssc.socketTextStream("127.0.0.1", 9999)

    // Break the individual words out of the lines rdd using a space as the delimiter
    val words = lines.flatMap(_.split(" "))
    // Map the lines rdd to a pair rdd with word and the number 1 as the pair
    val pairs = words.map(word => (word, 1))

    // Write the words to DSE/C* using a native connection. We are using this approach to support the use of our counter table.
    pairs.foreachRDD(rdd => rdd.foreach(line => {
      connector.withSessionDo(session => {
        session.execute(s"UPDATE spark_cass.stream_wc SET count=count+1 WHERE word=?", line._1)
      })
    }))

    // Use the Spark Streaming Windowing functionality to show the word count frequency for the last two minutes.
    val wordCounts = pairs.reduceByKeyAndWindow(_ + _, _ - _, Minutes(2), Seconds(2))
    wordCounts.print()

    // This initiates it all. Up to this point it was just meta-data for the rdd.
    ssc.start()
    ssc.awaitTermination()
  }
}

