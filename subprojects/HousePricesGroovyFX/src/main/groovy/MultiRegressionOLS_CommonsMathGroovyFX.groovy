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
import org.apache.commons.statistics.distribution.NormalDistribution
import org.apache.commons.math4.legacy.distribution.EmpiricalDistribution
import org.apache.commons.math4.legacy.stat.StatUtils
import org.apache.commons.math4.legacy.stat.regression.OLSMultipleLinearRegression

import static groovyx.javafx.GroovyFX.start
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

def features = [
        'bedrooms', 'bathrooms', 'sqft_living', 'sqft_living15', 'lat',
        'sqft_above', 'grade', 'view', 'waterfront', 'floors'
]

def idxs = 0..<features.size()
def priceIdx = features.size()
def file = getClass().classLoader.getResource('kc_house_data.csv').file
def csv  = CSV.withFirstRecordAsHeader().parse(new FileReader(file))

def all  = csv.collect { row -> [*idxs.collect{ row[features[it]].toDouble() }, row.price.toDouble()] }.findAll{ it[0] < 30 }
def (train, test) = all.chop(all.size() * 0.8 as int, -1)

def trainT = train.transpose()
def price = trainT[priceIdx]
def reg = new OLSMultipleLinearRegression().tap{ newSampleData(price as double[], trainT[0..<priceIdx].transpose() as double[][]) }
def params = reg.estimateRegressionParameters()
println params
def predicted = test.collect { data -> params[0] + (1..<params.size()).collect{ data[it-1] * params[it] }.sum() }
def residuals = test.indexed().collect { i, data -> predicted[i] - data[priceIdx] }
def rr = reg.calculateRSquared()
def rmseTrain = Math.sqrt(reg.calculateResidualSumOfSquares() / (train.size() - 1))
def rmseTest = Math.sqrt(StatUtils.sumSq(residuals as double[]) / (test.size() - 1))
def mean =  residuals.average()
println "$rr $rmseTrain $rmseTest $mean"
def showError = false
if (showError) {
    def maxError = [residuals.min(), residuals.max()].max { Math.abs(it) }
    residuals << maxError * -1 // make graph even around origin
    maxError = Math.abs(maxError)
    def step = maxError.toInteger() / 50
    def dist = EmpiricalDistribution.from(100, residuals as double[])
    def ndist = NormalDistribution.of(0, dist.sampleStats.standardDeviation)
    def bins = dist.binStats.indexed().collect { i, v -> [v.n ? v.mean.toInteger().toString() : (-maxError + i * step).toInteger().toString(), v.n] }
    def nbins = dist.binStats.indexed().collect { i, v -> def x = v.n ? v.mean.toInteger() : (-maxError + i * step).toInteger(); [x.toString(), ndist.probability(x, x + step)] }
    def scale = dist.binStats.max { it.n }.n / nbins.max { it[1] }[1]
    nbins = nbins.collect { [it[0], it[1] * scale] }
    start {
        stage(title: "Error histogram for ${features.join(', ')}", show: true, width: 800, height: 500) {
            scene {
                barChart(title: 'Error percentile', barGap: 0, categoryGap: 0) {
                    series(name: 'Error in prediction', data: bins)
                    series(name: 'Normal distribution', data: nbins)
                }
            }
        }
    }
} else {
    def from = [price.min(), predicted.min()].min()
    def to = [price.max(), predicted.max()].max()
    start {
        stage(title: "Price vs predicted", show: true, width: 900, height: 450) {
            scene {
//                scatterChart {
//                    series(name: 'Actual', data: [price, predicted].transpose())
//                }
                lineChart(stylesheets: resource('/style.css')) {
                    series(name: 'Actual', data: [price, predicted].transpose())
                    series(name: 'Ideal', data: [[from, from], [to, to]])
                }
            }
        }
    }
}
/* */
