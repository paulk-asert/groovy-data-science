//import StatUtils
import smile.regression.OLS
import tech.tablesaw.api.*
import groovyx.gpars.GParsPool

def features = [
        'bedrooms', 'bathrooms', 'sqft_living', 'sqft_living15', 'lat',
        'sqft_above', 'grade', 'view', 'waterfront', 'floors'
]

def idxs = 0..<features.size()
def file = getClass().classLoader.getResource('kc_house_data.csv').file
def table = Table.read().csv(file)
table = table.dropWhere(table.column("bedrooms").isGreaterThan(30))

def data = table.as().doubleMatrix(*features)
//def dataT = data.transpose()
//def price = dataT[priceIdx]
def model = new OLS(data, table.column('price').collect{ it } as double[])
println "Coefficients: ${model.coefficients()}"

GParsPool.withPool {
    def numbers = 1..4
    def maxPromise = numbers.inject(0, {a, b -> a>b?a:b}.asyncFun())

    println "Look Ma, I can talk to the user while the math is being done for me!"
    println maxPromise.get()
}

//def predicted = test.collect { data -> params[0] + (1..<params.size()).collect{ data[it-1] * params[it] }.sum() }
//def residuals = test.indexed().collect { i, data -> predicted[i] - data[priceIdx] }
//def rr = reg.calculateRSquared()
//def rmseTrain = Math.sqrt(reg.calculateResidualSumOfSquares() / (train.size() - 1))
//def rmseTest = Math.sqrt(StatUtils.sumSq(residuals as double[]) / (test.size() - 1))
//def mean = StatUtils.mean(residuals as double[])
//println "$rr $rmseTrain $rmseTest $mean"
