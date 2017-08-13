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

import java.util.Properties

import _root_.kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.log4j.{Level, Logger}
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object LoadFromKafkaStream {

  def main(args: Array[String]) {

    Logger.getRootLogger.setLevel(Level.WARN)

    /*
    * Variables for Cassandra connection
    */
    val keySpaceName =  "spark_cass"
    val fullTableName =  "stream_ts"
    val withAuth = false
    val password = "cassandra"
    val username = "cassandra"

    /*
     * Varialbles needed to connect to the Kafka Stream
     */
    //val Array(zkQuorum, group, topics, numThreads) = args
    val zkQuorum = "127.0.0.1:2181"
    val group = "Example"
    val topics = "stream_ts"
    val numThreads = "1"

    /*
     * Create the configuration for Spark Context
     */
    var sparkConf = new SparkConf().set("spark.cassandra.connection.host", "127.0.0.1")
      //.set("spark.cores.max", "2")
      //.set("spark.executor.memory", "512M")
      .set("spark.cleaner.ttl", "3600")
      .setJars(Array("target/scala-2.10/DSE-Spark-Streaming-assembly-1.0.jar"))
      .setMaster("spark://127.0.0.1:7077")
      .setAppName("DSE Spark Streaming From Kafka")
      .set("spark.eventLog.enabled", "true")
      .set("spark.eventLog.dir", "/Users/carybourgeois/Datastax/log/spark-events")
    if (withAuth){
      sparkConf = sparkConf
        .set("spark.cassandra.auth.username", username)
        .set("spark.cassandra.auth.password", password)
    }


    val connector = CassandraConnector(sparkConf)
    connector.withSessionDo(session => {
      session.execute(s"CREATE KEYSPACE IF NOT EXISTS ${keySpaceName} WITH REPLICATION = { 'class':'SimpleStrategy', 'replication_factor':1}")
      session.execute(s"DROP TABLE IF EXISTS ${keySpaceName}.${fullTableName}")
      session.execute(s"CREATE TABLE IF NOT EXISTS ${keySpaceName}.${fullTableName} (sensor_id int, event_time timestamp, metric double, PRIMARY KEY(sensor_id, event_time)) WITH CLUSTERING ORDER BY (event_time DESC)")
    })

    val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sc, Seconds(1))

    ssc.checkpoint("checkpoint")

    val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap
    val lines = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap).map(_._2)
    val fields = lines.map(_.split(";"))

    // This is where the lines from the queue get written to C*
    fields.foreachRDD(rdd => rdd.map(p => (p(0), p(1), p(2))).saveToCassandra(keySpaceName, fullTableName, SomeColumns("sensor_id", "event_time", "metric")))

    val sensorTotals = fields.map(p => (p(0), p(2).toDouble)).reduceByKeyAndWindow(_ + _, _ - _, Seconds(5), Seconds(1))
    sensorTotals.print()

    ssc.start()
    ssc.awaitTermination()
  }
}

// Produces some random words between 1 and 100.
object KafkaStreamProducer {

  def main(args: Array[String]) {

    Logger.getRootLogger.setLevel(Level.WARN)

    /*
     * Varialbles needed to connect to the Kafka Stream
     */
    //val Array(brokers, topic, messagesPerSec, wordsPerMessage) = args
    val brokers = "127.0.0.1:9092"
    val topic = "stream_ts"

    val numSensors = "5"
    val sigma = 10
    val xbar = 70

    // Zookeper connection properties
    val props = new Properties()
    props.put("metadata.broker.list", brokers)
    props.put("serializer.class", "kafka.serializer.StringEncoder")

    val config = new ProducerConfig(props)
    val producer = new Producer[String, String](config)

    // Send some messages
    while(true) {
      val messages = (1 to numSensors.toInt).map { sensorNum =>
        val sensor_id = sensorNum.toString
        val event_time = new java.sql.Timestamp(System.currentTimeMillis()).toString
        val metric = (scala.util.Random.nextGaussian() * sigma + xbar).toString
        val str = (s"${sensor_id};${event_time};${metric}")

        new KeyedMessage[String, String](topic, str)
      }.toArray

      producer.send(messages: _*)
      messages.foreach(println)
      Thread.sleep(1000)
    }
  }

}
