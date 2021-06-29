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
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import weka.classifiers.functions.SGD
import weka.core.converters.CSVLoader
import weka.filters.Filter
import weka.filters.unsupervised.attribute.Remove

import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Line
import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Scatter
import static org.knowm.xchart.style.markers.SeriesMarkers.NONE

def file = getClass().classLoader.getResource('kc_house_data.csv').file as File

def loader = new CSVLoader(file: file)
def model = new SGD()
model.options = ['-F', '4', '-N'] as String[] // Huber loss, unscaled
def allInstances = loader.dataSet
def priceIndex = 2
allInstances.classIndex = priceIndex
// remove "id", "date", 'zip', 'lat', 'long' columns
def rm = new Remove(attributeIndices: '1,2,17,18,19', inputFormat: allInstances)
def instances = Filter.useFilter(allInstances, rm)
model.buildClassifier(instances)
println model

def actual = instances.collect{ it.value(0).toDouble() }
def predicted = instances.collect{ model.classifyInstance(it) }

def chart = new XYChartBuilder().width(900).height(450).title("Actual vs predicted price").xAxisTitle("Actual").yAxisTitle("Predicted").build()
chart.addSeries("Price", actual as double[], predicted as double[]).with {
    XYSeriesRenderStyle = Scatter
}
def from = [actual.min(), predicted.min()].min()
def to = [actual.max(), predicted.max()].min()
chart.addSeries("Ideal", [from, to] as double[], [from, to] as double[]).with {
    marker = NONE
    XYSeriesRenderStyle = Line
}
new SwingWrapper(chart).displayChart()
