import smile.regression.OLS
import tech.tablesaw.api.*
import groovyx.gpars.GParsPool

import static java.lang.Math.sqrt
import static org.apache.commons.math3.stat.StatUtils.sumSq
import static smile.math.Math.dot

def features = [
        'price', 'bedrooms', 'bathrooms', 'sqft_living', 'sqft_living15', 'lat',
        'sqft_above', 'grade', 'view', 'waterfront', 'floors'
]

def file = getClass().classLoader.getResource('kc_house_data.csv').file
def table = Table.read().csv(file)
table = table.dropWhere(table.column("bedrooms").isGreaterThan(30))
def data = table.as().doubleMatrix(*features)

GParsPool.withPool {
    def trainChunkSize = 3000
    def numTrainChunks = 8
    def models = (0..<numTrainChunks).collectParallel {
        def list = data.toList().shuffled().take(trainChunkSize)
        new OLS(list.collect{ it[1..-1] } as double[][], list.collect{ it[0] } as double[]).with{ [it.intercept(), *it.coefficients()] }
    }
//    models.each {
//        println "Intercept: ${it[0]}, Coefficients: ${it[1..-1]}"
//    }
    def model = models.transpose()*.sum().collect{ it/numTrainChunks }
    println "Intercept: ${model[0]}"
    println "Coefficients: ${model[1..-1].join(', ')}"

    double[] coefficients = model[1..-1]
    double intercept = model[0]
    def stats = { chunk ->
        def predicted = chunk.collect { row -> intercept + dot(row[1..-1] as double[], coefficients) }
        def residuals = chunk.toList().indexed().collect { idx, row -> predicted[idx] - row[0] }
        def rmse = sqrt(sumSq(residuals as double[]) / chunk.size())
        [rmse, residuals.average(), chunk.size()]
    }
//    println stats(data)
    def evalChunkSize = 2000
    def results = data.collate(evalChunkSize).collectParallel(stats)
    println 'RMSE: ' + sqrt(results.collect{ it[0] * it[0] * it[2] }.sum() / data.size())
    println 'mean: ' + results.collect{ it[1] * it[2] }.sum() / data.size()
}
