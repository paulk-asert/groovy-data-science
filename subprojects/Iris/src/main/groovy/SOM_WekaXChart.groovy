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
import weka.core.Instances
import weka.core.converters.CSVLoader

import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Scatter

def file = getClass().classLoader.getResource('iris_data.csv').file as File

def loader = new CSVLoader(file: file)
def somClass
try {
    somClass = Class.forName("weka.clusterers.SelfOrganizingMap")
} catch(ex) {
    println "Optional Weka package 'SelfOrganizingMap' not found: $ex.message"
}

def model = somClass.getConstructor().newInstance()
def allInstances = loader.dataSet
model.buildClusterer(allInstances)
println model

def chart = new XYChartBuilder().width(900).height(450).
        title("Species").xAxisTitle("Petal length").yAxisTitle("Petal width").build()
model.clusterInstances.eachWithIndex { Instances instances, int i ->
    println "Cluster $i:\n" + instances.join('\n')
    chart.addSeries("Cluster $i", instances.attributeToDoubleArray(0), instances.attributeToDoubleArray(1)).with {
        XYSeriesRenderStyle = Scatter
    }
}
new SwingWrapper(chart).displayChart()
