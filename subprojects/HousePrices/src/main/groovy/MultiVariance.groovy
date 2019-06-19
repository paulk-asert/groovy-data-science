import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression
//import static groovyx.javafx.GroovyFX.start
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

def file = getClass().classLoader.getResource('kc_house_data.csv').file
def csv  = CSV.withFirstRecordAsHeader().parse(new FileReader(file))
def all  = csv.toList()
def price = all.collect{ it[2].toDouble() }
def other = all.collect{ it.toList()[3..-1]*.toDouble() }
println price[0]
println other[0]
def reg = new OLSMultipleLinearRegression()
reg.newSampleData(price as double[], other as double[][])

def beta = reg.calculateBeta()
def predicted = other.collect{ (0..<it.size()).collect{ i -> beta.getEntry(i) * it[i] }.sum() }
println ((0..5).collect{ beta.getEntry(it) })
println price[0..5]
println predicted[0..5]

/*
start {
    stage(title: 'Price vs Number of bedrooms', show: true, width: 800, height: 600) {
        scene {
//            scatterChart(opacity: 50) {
//                series(name: 'Actual', data: all)
//            }
//            lineChart {
            lineChart(stylesheets: resource('/style.css')) {
                series(name: 'Actual', data: all)
                series(name: 'Predicted', data: predicted)
            }
        }
    }
}
/* */
