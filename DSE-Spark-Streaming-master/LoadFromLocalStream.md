#Overview
The goal of this exercise is to create a very minimal Spark Streaming example. The example is the obligatory "Big Data" word count example with a streaming twist. You will create a Scala/Spark program that use Spark Streaming to pull data from a local TextStream. It will then split the lines of the stream into individual words and save those into a DSE/C* counter table. This will provide a total occurrence view of all the words coming across the stream. The program then used the windowing functions in Spark Streaming to output the occurrences of words that have happened in the last two minutes.

In this exercise will perform the following activities:

1. Verify local DSE version. Start DSE/cassandra with Spark enabled.
2. Validate that sbt is installed and available
3. Clone this GitHub repository to your local machine
4. Locate/Review/Modify the Scala code for this example
5. Prepare the command Windows for the example
6. Execute/Interact with the example.

#Requirements

For this part of the Exercise you will need the following
  * Local DSE 4.6+ installed locally on either an OS X or Linux environment
  * git installed locally and access to GitHub: [GitHub Site](https://github.com/)
  * sbt installed locally: [sbt site](http://www.scala-sbt.org/)

##1. Verify local DSE version. Start DSE/cassandra with Spark enabled.

  * The first thing you need to do is to validate that you have and can run DSE 4.6 locally.

    * Navigate to the root directory of your DSE install and execute the command `bin/dse -v`. The result should be `4.6.0` or newer.

  * Next, start DSE with Spark enabled.
    * Again from the root directory of your DSE install, execute the command `bin/dse cassandra -k`. This will start the local copy of DSE with Spark enabled.
  * You can verify Spark is enabled by initiating the DSE/Spark REPL.
    * Again from the root directory of your DSE install, execute the command `bin/dse spark`. You should see something like the screen below.

        Welcome to
              ____              __
             / __/__  ___ _____/ /__
            _\ \/ _ \/ _ `/ __/  '_/
           /___/ .__/\_,_/_/ /_/\_\   version 1.1.0
              /_/

        Using Scala version 2.10.4 (Java HotSpot(TM) 64-Bit Server VM, Java 1.7.0_75)
        Type in expressions to have them evaluated.
        Type :help for more information.
        Creating SparkContext...
        Initializing SparkContext with MASTER: spark://127.0.0.1:7077
        Created spark context..
        Spark context available as sc.
        HiveSQLContext available as hc.
        CassandraSQLContext available as csc.
        Type in expressions to have them evaluated.
        Type :help for more information.

        scala>

##2. Validate that sbt is installed and available

We use the Scala Build Tool (sbt) to build and run the code in this exercise. For this to work sbt must be installed and on the executable path of your system.

To validate sbt is installed on your system you should be able to go to the command line and execute the following commands and get these or similar results.

        $>sbt sbt-version
        [info] Set current project to bin (in build file:/Users/carybourgeois/bin/)
        [info] 0.13.5
If this is not the case then please visit the [sbt site](http://www.scala-sbt.org/) for instructions on how to download and install sbt.

##3. Clone this GitHub repository to your local machine

In order to work with this exercise and the remaining exercises in this example you should clone the git repository to the machine where DSE is located.

Navigate to a directory that you would like to use for this project. From the command line in that directory issue the following command

                git clone https://github.com/CaryBourgeois/DSE-Spark-Streaming.git

##4. Locate/Review/Modify the Scala code for this example

This project was created with the Community Edition of IntelliJ IDEA. The simplest way to review and modify the scala code is to open the project with this IDE. Alternatively, you can use any text editor to view/edit the file as the build and execute process will work from the command line via sbt.

From the directory where you cloned the GitHub project, navigate to the `/src/main/scala` directory. Locate and open the file `LoadFromLocalStream.scala`.

This is a very simple Scala/Spark example. It contain one object and `main` method in that object. Within that method there are a number of important segments of the code:

  * Setup the connection to Spark
    * Specify the Spark configuration parameters (These will have to be modified to fit your environment)
    * Pay special attention the "spark.cores.max" parameter. This must be at least 2 for a Spark Streaming app.
  * Prepare the Cassandra keyspace and tables for the new data
    * Obtain a native connection to Cassandra
    * Verify/create the keyspace on the cluster
    * Drop tables if they already exist and create new tables to receive the data
  * Create the SparkStreamingContext (ssc)
    * Set the frequency that Spark Streaming will check the channel
    * Set the SparkStreaming "checkpoint" directory. This allows Spark Streaming to recover from failed jobs.
  * Create a Spark Streaming Text Stream
    * in the program we create an streaming RDD from the `socketTextStream` command.
    * In this case we will use the nc (netcat) command to create a local text stream in a terminal window
  * Manipulate the stream contents
    * Split each line of the stream into individual words
    * Create a pair RDD with each word and the value 1 as a pair. This allows us to sum/count word occurrences later.
  * Update the DSE/C* Counter table with the words in the pair RDD
  * Use Spark Streaming functionality to show a window of time for the objects in the stream.
    * The `reduceByKeyAndWindow` function allows us to specify the time window to use for the new RDD.
  * Start the streaming process with the call to `ssc.start()`

Once you have reviewed the code you will need to make changes to reflect your specific system. If your specific system does not loopback to `127.0.0.1`, you will need to go through the code and replace `127.0.0.1` with your ip address. Once you have done that save the file and you are ready for the next step.

##5. Prepare the command Windows for the example

To appreciate this demo you will want to set up three command windows.

  1. A command window running cqlsh
  2. A command window where you have navigated to the root directory of GitHub project.
  3. A command window where you have executed the command: `nc -lk 9999`

##6. Execute/Interact with the example.

In this step we will build and run the code. We will also create messages on the queue and visualize them via the application window. We will also see how the data was stored in C*.

In command window 2, the one at the root of the GitHub project, execute the following commands:
 * `sbt assembly` - you should see a `[success]` in the output
 * `sbt run` You will get a menu that looks something like the one below:

    Multiple main classes detected, select one to run:

     [1] LoadFromKafkaStream
     [2] KafkaStreamProducer
     [3] LoadFromLocalStream

    Enter number:
  * Select the option attached to `LoadFromLocalStream` in this case option 3. It will take a few seconds but you will see out put like this updating every few seconds:

        [info] Running LoadFromLocalStream
        -------------------------------------------
        Time: 1423166210000 ms
        -------------------------------------------

        -------------------------------------------
        Time: 1423166212000 ms
        -------------------------------------------

        -------------------------------------------
        Time: 1423166214000 ms
        -------------------------------------------

In command window 3, start typing strings of words followed by a return. This is the stream that will be passed to Spark. the Window should look something like this:

        caryb-rmbp15:~$ nc -lk 9999
        This is a test
        Isn't Spark Streaming a hott
        hoot
        I can't believe how easy this was to put together!

In command window 2 you should have output that looks like this:

        -------------------------------------------
        Time: 1423175634000 ms
        -------------------------------------------
        (this,1)
        (is,1)
        (believe,1)
        (hoot,1)
        (how,1)
        (This,1)
        (easy,1)
        (Streaming,1)
        (test,1)
        (Spark,1)
        ...

Switch to command window 1, the one with cqlsh running, and run the following CQL command

        SELECT * FROM spark_cass.stream_wc;

You should see out put like:

        Connected to Test Cluster at localhost:9160.
        [cqlsh 4.1.1 | Cassandra 2.0.11.83 | DSE 4.6.0 | CQL spec 3.1.1 | Thrift protocol 19.39.0]
        Use HELP for help.
        cqlsh> SELECT * FROM spark_cass.stream_wc;

         word      | count
        -----------+-------
              hoot |     1
                 a |     2
               put |     1
              this |     1
              test |     1
                is |     1
              This |     1
                to |     1
              easy |     1
               was |     1
               how |     1
              hott |     1
             Spark |     1
             can't |     1
         Streaming |     1
                 I |     1
         together! |     1
           believe |     1
             Isn't |     1

        (19 rows)

Thats it! You now how a fully functional Spark Streaming application that is integrated with DSE/C*. ENJOY!