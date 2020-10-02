//@Grab("com.fasterxml.jackson.module:jackson-module-scala_2.11:2.11.1;transitive=false")
//@Grab('org.apache.spark:spark-sql_2.11:2.4.7')
//@Grab('org.apache.spark:spark-mllib_2.11:2.4.7')
//@GrabExclude("commons-codec:commons-codec:1.10")
//@GrabExclude("javax.xml.stream:stax-api:1.0-2")
//@Grab("commons-io:commons-io:2.6")
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import static org.apache.spark.sql.SparkSession.builder

static method() {

def spark = builder().config('spark.master', 'local[8]').appName('Whiskey').orCreate
def file = WhiskeySpark.classLoader.getResource('whiskey.csv').file
//def file = '/path/to/whiskey.csv'
int k = 5
Dataset<Row> rows = spark.read().format('com.databricks.spark.csv')
        .options('header': 'true', 'inferSchema': 'true').load(file)
//def colNames = rows.columns().toList().minus(extras).parallelStream().toArray(String[]::new)
String[] colNames = rows.columns().toList().minus(['RowID', 'Distillery'])
def assembler = new VectorAssembler(inputCols: colNames, outputCol: 'features')
Dataset<Row> dataset = assembler.transform(rows)
def clusterer = new KMeans(k: k, seed: 1L)
def model = clusterer.fit(dataset)
println 'Cluster centers:'
model.clusterCenters().each{ println it.values().collect{ sprintf '%.2f', it }.join(', ') }
spark.stop()

}

method()
/*
Cluster centers:
1.73, 2.35, 1.58, 0.81, 0.19, 1.15, 1.42, 0.81, 1.23, 1.77, 1.23, 1.31
2.00, 1.00, 3.00, 0.00, 0.00, 0.00, 3.00, 1.00, 0.00, 2.00, 2.00, 2.00
2.86, 2.38, 1.52, 0.05, 0.00, 1.95, 1.76, 2.05, 1.81, 2.05, 2.19, 1.71
1.53, 2.38, 1.06, 0.16, 0.03, 1.09, 1.00, 0.50, 1.53, 1.75, 2.13, 2.28
3.67, 1.50, 3.67, 3.33, 0.67, 0.17, 1.67, 0.50, 1.17, 1.33, 1.17, 0.17
 */