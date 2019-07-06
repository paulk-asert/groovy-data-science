import scala.Tuple2
import static org.apache.spark.sql.SparkSession.builder

static method() {

def PUNCT  = ~"[ ,.?]"
def spark  = builder().config('spark.master', 'local').appName('WordCount').orCreate
def lines  = spark.read().textFile('/path/to/peppers.txt').javaRDD()
def words  = lines.flatMap(s -> PUNCT.split(s).iterator())
def ones   = words.mapToPair(s -> new Tuple2<>(s, 1))
def counts = ones.reduceByKey{ t1, t2 -> t1 + t2 }
def output = counts.collect()
for (tuple in output) {
    println tuple._1() + ": " + tuple._2()
}
spark.stop()

}

method()
