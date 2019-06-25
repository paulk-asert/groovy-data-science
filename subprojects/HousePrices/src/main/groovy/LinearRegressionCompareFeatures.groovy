import groovy.transform.Canonical
import groovyx.javafx.beans.FXBindable
import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.random.EmpiricalDistribution
import org.apache.commons.math3.stat.StatUtils
import org.apache.commons.math3.stat.regression.SimpleRegression

import static groovyx.javafx.GroovyFX.start
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

@Canonical
@FXBindable
class Feature {
    String name
    double rr, rmseTrain, rmseTest, mean
}

def features = [
        new Feature('bedrooms'),
        new Feature('bathrooms'),
        new Feature('sqft_living')
]

def idxs = 0..<features.size()
def priceIdx = features.size()
def file = getClass().classLoader.getResource('kc_house_data.csv').file
def csv  = CSV.withFirstRecordAsHeader().parse(new FileReader(file))

def all  = csv.collect { row -> [*idxs.collect{ row[features[it].name].toDouble() }, row.price.toDouble()] }.findAll{ it[0] < 30 }
def (train, test) = all.chop(all.size() * 0.8 as int, -1)

def trainT = train.transpose()
def regs = idxs.collect { idx ->
    new SimpleRegression().tap{ addData([trainT[idx], trainT[priceIdx]].transpose() as double[][]) }
}
def residuals = idxs.collect{ idx -> test.collect{ regs[idx].predict(it[idx]) - it[priceIdx] } }
def graphIdx = -1
if (graphIdx == -1) {
    idxs.each { idx ->
        features[idx].rr = regs[idx].RSquare
        features[idx].rmseTrain = Math.sqrt(regs[idx].meanSquareError)
        features[idx].rmseTest = Math.sqrt(StatUtils.sumSq(residuals[idx] as double[]) / (test.size() - 1))
        features[idx].mean = StatUtils.mean(residuals[idx] as double[])
    }
    start {
        stage(title: "Error statistics for feature", visible: true) {
            scene(fill: groovyblue, width: 800, height:200) {
                stackPane(padding: 10) {
                    tableView(items: features) {
                        tableColumn(text: "Feature", property: 'name')
                        tableColumn(text: "R²", property: 'rr')
                        tableColumn(text: "RMSE (train)", property: 'rmseTrain')
                        tableColumn(text: "RMSE (test)", property: 'rmseTest')
                        tableColumn(text: "Residuals Mean", property: 'mean')
                    }
                }
            }
        }
    }
} else {
    def maxError = [residuals[graphIdx].min(), residuals[graphIdx].max()].max{ Math.abs(it) }
    residuals[graphIdx] << maxError * -1 // make graph even around origin
    maxError = Math.abs(maxError)
    def step = maxError.toInteger() / 50
    def dist = new EmpiricalDistribution(100).tap{ load(residuals[graphIdx] as double[]) }
    def ndist = new NormalDistribution(0, dist.sampleStats.standardDeviation)
    def bins = dist.binStats.indexed().collect { i, v -> [v.n ? v.mean.toInteger().toString(): (-maxError + i * step).toInteger().toString(), v.n] }
    def nbins = dist.binStats.indexed().collect { i, v -> def x = v.n ? v.mean.toInteger() : (-maxError + i * step).toInteger(); [x.toString(), ndist.probability(x, x + step)] }
    def scale = dist.binStats.max{ it.n }.n / nbins.max{ it[1] }[1]
    nbins = nbins.collect{ [it[0], it[1] * scale] }
    start {
        stage(title: "Error histogram for ${features[graphIdx].name}", show: true, width: 800, height: 600) {
            scene {
                barChart(title: 'Error percentile', barGap: 0, categoryGap: 0) {
                    series(name: 'Error in prediction', data: bins)
                    series(name: 'Normal distribution', data: nbins)
                }
            }
        }
    }
}
/* */
