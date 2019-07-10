import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import org.apache.spark.ml.feature.VectorAssembler
import static org.apache.spark.sql.SparkSession.builder

static method() {

def spark = builder().config('spark.master', 'local[8]').appName('Whiskey').orCreate
def file = WhiskeySpark.classLoader.getResource('whiskey.csv').file
//def file = '/path/to/whiskey.csv'
int k = 5
Dataset<Row> rows = spark.read().format('com.databricks.spark.csv').option('header', true).option('inferSchema', true).load(file)
ArrayList<String> colNamesList = new ArrayList<String>(Arrays.asList(rows.columns()))
colNamesList.remove('RowID')
String[] colNames = colNamesList.parallelStream().toArray(String[]::new)
def assembler = new VectorAssembler(inputCols: colNames, outputCol: 'features')
Dataset<Row> dataset = assembler.transform(rows)
def clusterer = new KMeans(k: k, seed: 1L)
def model = clusterer.fit(dataset)
double SSE = model.computeCost(dataset)
println model.summary()
println "Sum of Squared Errors = $SSE"
//for (tuple in output) {
//    println tuple._1() + ": " + tuple._2()
//}
spark.stop()

}

method()
