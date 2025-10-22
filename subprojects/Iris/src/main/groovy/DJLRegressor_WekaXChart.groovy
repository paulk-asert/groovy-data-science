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
import weka.classifiers.AbstractClassifier
import weka.core.Utils
import weka.core.WekaPackageManager
import weka.core.converters.CSVLoader

import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Scatter

WekaPackageManager.loadPackages(true)

def file = getClass().classLoader.getResource('iris_data.csv').file as File
println file
def species = ['Iris-setosa', 'Iris-versicolor', 'Iris-virginica']
def loader = new CSVLoader(file: file)
def data = loader.dataSet
data.classIndex = 4

def options = Utils.splitOptions("-S 1")
AbstractClassifier classifier = Utils.forName(AbstractClassifier, "weka.classifiers.djl.DJLRegressor", options)

double[] actual = data.collect{ it.value(4) }
double[] predicted = data.collect{ classifier.classifyInstance(it) }
double[] petalW = data.collect{ it.value(2) }
double[] petalL = data.collect{ it.value(3) }
def indices = actual.indices

def chart = new XYChartBuilder().width(900).height(450).
    title("Species").xAxisTitle("Petal length").yAxisTitle("Petal width").build()
species.eachWithIndex{ String name, int i ->
    def groups = indices.findAll{ predicted[it] == i }.groupBy{ actual[it] == i }
    Collection found = groups[true] ?: []
    Collection errors = groups[false] ?: []
    println "$name: ${found.size()} correct, ${errors.size()} incorrect"
    chart.addSeries("$name correct", petalW[found], petalL[found]).with {
        XYSeriesRenderStyle = Scatter
    }
    if (errors) {
        chart.addSeries("$name incorrect", petalW[errors], petalL[errors]).with {
            XYSeriesRenderStyle = Scatter
        }
    }
}
new SwingWrapper(chart).displayChart()
