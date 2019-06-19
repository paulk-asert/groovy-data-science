//@Grab('org.slf4j:slf4j-simple:1.7.25')
//@Grab('org.slf4j:slf4j-api:1.7.25')
//@Grab('org.apache.spark:spark-core_2.12:2.4.3')
//@Grab('org.apache.spark:spark-sql_2.12:2.4.3')
//@Grab('org.apache.spark:spark-streaming_2.12:2.4.3')
//@Grab('org.apache.spark:spark-mllib_2.12:2.4.3')
//@Grab('org.apache.spark:spark-catalyst_2.12:2.4.3')
//@Grab('org.apache.spark:spark-network-common_2.12:2.4.3')
//@Grab('org.scala-lang:scala-library:2.12.8')
//@Grab('org.apache.hadoop:hadoop-auth:2.4.1')
//@GrabExclude('org.tukaani:xz')

// download spark 2.11_2.4.3 and point to all jars in the jars folder
// exclude Groovy's json module as the jackson dependency conflicts

import scala.Tuple2
import org.apache.spark.sql.SparkSession

static method() {
def SPACE  = ~" "
def spark  = SparkSession.builder()
             .config("spark.master", "local")
             .appName("JavaWordCount")
             .getOrCreate()
def lines  = spark.read().textFile('/tmp/peppers.txt').javaRDD()
//def lines  = spark.read().textFile('C:/Temp/fox.txt').javaRDD()
def words  = lines.flatMap(s -> SPACE.split(s).iterator())
def ones   = words.mapToPair(s -> new Tuple2<>(s, 1))
def counts = ones.reduceByKey{ i1, i2 -> i1 + i2 }
def output = counts.collect()
for (tuple in output) {
    println tuple._1() + ": " + tuple._2()
}
spark.stop()
}

method()
