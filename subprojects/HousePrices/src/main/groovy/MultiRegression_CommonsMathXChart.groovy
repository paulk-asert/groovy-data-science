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
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder

import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV
import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Line
import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Scatter
import static org.knowm.xchart.style.markers.SeriesMarkers.NONE

def file = getClass().classLoader.getResource('kc_house_data.csv').file
def csv  = CSV.withFirstRecordAsHeader().parse(new FileReader(file))
def all  = csv.toList()
def price = all.collect{ it[2].toDouble() }
def features = all.collect{ it.toList()[3..-1]*.toDouble() }
def start = System.currentTimeMillis()
def reg = new OLSMultipleLinearRegression()
reg.newSampleData(price as double[], features as double[][])
def betas = reg.estimateRegressionParameters()
def end = System.currentTimeMillis()
println end - start
def predicted = features.collect{ row -> row.indices.collect{ i -> betas[i+1] * row[i] }.sum() + betas[0] }

def chart = new XYChartBuilder().width(900).height(450).title("Actual vs predicted price").xAxisTitle("Actual").yAxisTitle("Predicted").build()
chart.addSeries("Price", price as double[], predicted as double[]).with {
    XYSeriesRenderStyle = Scatter
}
def from = [price.min(), predicted.min()].min()
def to = [price.max(), predicted.max()].max()
chart.addSeries("Ideal", [from, to] as double[], [from, to] as double[]).with {
    marker = NONE
    XYSeriesRenderStyle = Line
}
new SwingWrapper(chart).displayChart()
