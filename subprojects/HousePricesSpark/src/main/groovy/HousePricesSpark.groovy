/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//@Grab("com.fasterxml.jackson.module:jackson-module-scala_2.11:2.11.1;transitive=false")
//@Grab('org.apache.spark:spark-sql_2.11:2.4.7')
//@Grab('org.apache.spark:spark-mllib_2.11:2.4.7')
//@GrabExclude("commons-codec:commons-codec:1.10")
//@GrabExclude("javax.xml.stream:stax-api:1.0-2")
//@Grab("commons-io:commons-io:2.6")

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row

import static org.apache.spark.sql.SparkSession.builder

static main(args) {
    def spark = builder().config('spark.master', 'local[8]').appName('HousePrices').orCreate
    spark.sparkContext().logLevel = 'WARN'
    def file = HousePricesSpark.classLoader.getResource('kc_house_data.csv').file
    int k = 5
    Dataset<Row> ds = spark.read().format('csv')
        .options('header': 'true', 'inferSchema': 'true')
        .load(file)
    double[] splits = [80, 20]
    def (training, test) = ds.randomSplit(splits)

    String[] colNames = ds.columns().toList() - ['id', 'date', 'price']
    def assembler = new VectorAssembler(inputCols: colNames, outputCol: 'features')
    Dataset<Row> dataset = assembler.transform(training)
    def lr = new LinearRegression(labelCol: 'price', maxIter: 10)
    def model = lr.fit(dataset)
    println '\nCoefficients:'
    println model.coefficients().values()[1..-1]
        .collect { sprintf '%.2f', it }.join(', ')
    def testSummary = model.evaluate(assembler.transform(test))
    printf 'RMSE: %.2f%n', testSummary.rootMeanSquaredError
    printf 'r2: %.2f%n%n', testSummary.r2
    spark.sparkContext().logLevel = 'INFO'
    spark.stop()
}
/*
41979.78, 80853.89, 0.15, 5412.83, 564343.22, 53834.10, 24817.09, 93195.29, -80662.68, -80694.28, -2713.58, 19.02, -628.67, 594468.23, -228397.19, 21.23, -0.42
RMSE: 187242.12
r2: 0.70
 */
