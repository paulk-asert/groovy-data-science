import org.apache.commons.math3.stat.regression.SimpleRegression
import static groovyx.javafx.GroovyFX.start
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

def file = getClass().classLoader.getResource('kc_house_data.csv').file
def csv  = CSV.withFirstRecordAsHeader().parse(new FileReader(file))
def all  = csv.collect { [it.bedrooms.toDouble(), it.price.toDouble()] }.findAll{ it[0] < 30 }

def reg = new SimpleRegression()
reg.addData(all as double[][])

def (minB, maxB) = [all.min { it[0] }[0], all.max { it[0] }[0]]
def predicted = [[minB, reg.predict(minB)], [maxB, reg.predict(maxB)]]

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
