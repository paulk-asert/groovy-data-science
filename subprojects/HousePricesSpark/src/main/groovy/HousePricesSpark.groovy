import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import static org.apache.spark.sql.SparkSession.builder

static method() {

def spark = builder().config('spark.master', 'local[8]').appName('HousePrices').orCreate
def file = HousePricesSpark.classLoader.getResource('kc_house_data.csv').file
//def file = '/path/to/kc_house_data.csv'
int k = 5
Dataset<Row> ds = spark.read().format('csv')
        .options('header': 'true', 'inferSchema': 'true').load(file)
double[] splits = [80, 20]
def (training, test) = ds.randomSplit(splits)

String[] colNames = ds.columns().toList().minus(['id', 'date', 'price'])
def assembler = new VectorAssembler(inputCols: colNames, outputCol: 'features')
Dataset<Row> dataset = assembler.transform(training)
def lr = new LinearRegression(labelCol: 'price', maxIter: 10)
def model = lr.fit(dataset)
println 'Coefficients:'
println model.coefficients().values()[1..-1].collect{ sprintf '%.2f', it }.join(', ')
def testSummary = model.evaluate(assembler.transform(test))
printf 'RMSE: %.2f%n', testSummary.rootMeanSquaredError
printf 'r2: %.2f%n', testSummary.r2
spark.stop()

}

method()
/*
41979.78, 80853.89, 0.15, 5412.83, 564343.22, 53834.10, 24817.09, 93195.29, -80662.68, -80694.28, -2713.58, 19.02, -628.67, 594468.23, -228397.19, 21.23, -0.42
RMSE: 187242.12
r2: 0.70
 */