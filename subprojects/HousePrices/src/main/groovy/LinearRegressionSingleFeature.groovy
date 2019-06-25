import org.apache.commons.math3.stat.regression.SimpleRegression
import static groovyx.javafx.GroovyFX.start
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

def feature = 'bedrooms'
def file = getClass().classLoader.getResource('kc_house_data.csv').file
def csv  = CSV.withFirstRecordAsHeader().parse(new FileReader(file))
def all  = csv.collect { [it[feature].toDouble(), it.price.toDouble()] }//.findAll{ it[0] < 30 }

def reg = new SimpleRegression()
reg.addData(all as double[][])

def (min, max) = all.transpose().with{ [it[0].min(), it[0].max()] }
def predicted = [[min, reg.predict(min)], [max, reg.predict(max)]]

start {
    stage(title: "Price vs $feature", show: true, width: 800, height: 600) {
        scene {
//            NOTE using css trick to allow multiple chart types
//            TODO consider using JavaFXMultiChart
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
