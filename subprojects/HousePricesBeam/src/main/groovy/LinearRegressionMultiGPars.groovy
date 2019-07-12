import org.apache.commons.math3.stat.StatUtils
import smile.math.Math
import smile.math.distance.EuclideanDistance
import smile.regression.OLS
import tech.tablesaw.api.*
import groovyx.gpars.GParsPool

import static java.lang.Math.sqrt

def features = [
        'price', 'bedrooms', 'bathrooms', 'sqft_living', 'sqft_living15', 'lat',
        'sqft_above', 'grade', 'view', 'waterfront', 'floors'
]

def idxs = 0..<features.size()
def file = getClass().classLoader.getResource('kc_house_data.csv').file
def table = Table.read().csv(file)
table = table.dropWhere(table.column("bedrooms").isGreaterThan(30))
def data = table.as().doubleMatrix(*features)

GParsPool.withPool {
    def trainChunkSize = 5000
    def numTrainChunks = 5
    def models = (0..<numTrainChunks).collectParallel {
        def list = data.toList()
        Collections.shuffle(list)
        list = list.take(trainChunkSize)
        new OLS(list.collect{ it[1..-1] } as double[][], list.collect{ it[0] } as double[]).with{ [it.intercept(), *it.coefficients()] }
    }
    models.each {
        println "Intercept: ${it[0]}, Coefficients: ${it[1..-1]}"
    }
    def model = models.transpose()*.sum().collect{ it/numTrainChunks }
    println "Averaged =>\nIntercept: ${model[0]}, Coefficients: ${model[1..-1]}"

    double[] coefficients = model[1..-1]
    double intercept = model[0]
    double[] predicted = data.collect { row -> intercept + Math.dot(row[1..-1] as double[], coefficients) }
    double[] residuals = data.toList().indexed().collect { idx, row -> predicted[idx] - row[0] }
    def rmse = sqrt(StatUtils.sumSq(residuals as double[]) / data.size())
    def mean = Math.mean(residuals as double[])
    println "$rmse $mean"
    def evalChunkSize = 2000
    def results = data.collate(evalChunkSize).collectParallel { chunk ->
        predicted = chunk.collect { row -> intercept + Math.dot(row[1..-1] as double[], coefficients) }
        residuals = chunk.indexed().collect { idx, row -> predicted[idx] - row[0] }
        rmse = sqrt(StatUtils.sumSq(residuals as double[]) / chunk.size())
        mean = Math.mean(residuals as double[])
        [rmse, mean, chunk.size()]
    }
    results.each {
        println it
    }
    def resultsT = results.transpose()
    println sqrt(resultsT.collect{ (it[0] * it[0] * it[2]) }.sum() / data.size())
}
