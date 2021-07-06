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

import com.datumbox.framework.common.Configuration
import com.datumbox.framework.core.common.dataobjects.Dataframe
import com.datumbox.framework.core.machinelearning.MLBuilder
import com.datumbox.framework.core.machinelearning.classification.SupportVectorMachine

import com.datumbox.framework.core.machinelearning.modelselection.metrics.ClassificationMetrics
import com.datumbox.framework.core.machinelearning.modelselection.splitters.ShuffleSplitter
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder

import static com.datumbox.framework.common.dataobjects.TypeInference.DataType.CATEGORICAL
import static com.datumbox.framework.common.dataobjects.TypeInference.DataType.NUMERICAL
import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Scatter

def file = getClass().classLoader.getResource('iris_data.csv').file as File

def cols = ['Sepal length', 'Sepal width', 'Petal length', 'Petal width']
// RandomGenerator.globalSeed = -1L // for repeatable results
def config = Configuration.configuration
def headers = [*: cols.collectEntries { [it, NUMERICAL] }, Class: CATEGORICAL]
Dataframe data = null
def defaultSeps = [',' as char, '"' as char, "\r\n"]
file.withReader {
    data = Dataframe.Builder.parseCSVFile(it, 'Class', headers, *defaultSeps, null, null, config)
}

def split = new ShuffleSplitter(0.8, 1).split(data).next()
Dataframe training = split.train
Dataframe testing = split.test

def classifierParams = new SupportVectorMachine.TrainingParameters()
SupportVectorMachine classifier = MLBuilder.create(classifierParams, config)
classifier.fit(training)
classifier.save("Class")
classifier.predict(testing)

println "Results:"
def petalL = [:].withDefault{[]}
def petalW = [:].withDefault{[]}
testing.entries().each {
    def key = it.key
    def predicted = it.value.YPredicted
    def correct = it.value.y == it.value.YPredicted
    def probs = it.value.YPredictedProbabilities
    def prefix = { it == predicted ? (correct ? '*' : '**') : '' }
    def series = correct ? it.value.y : "$it.value.YPredicted/$it.value.y"
    petalL[series] << it.value.x.get('Petal length')
    petalW[series] << it.value.x.get('Petal width')
    def probsForClass = { klass -> prefix(klass) + sprintf('%5.3f', probs.get(klass)) }
    def probability = ['Iris-setosa', 'Iris-versicolor', 'Iris-virginica'].collect(probsForClass)
    println "Record $key - Actual: $it.value.y, Predicted: $predicted (probabilities: $probability)"
}

def metrics = new ClassificationMetrics(testing)
println "Classifier Accuracy: $metrics.accuracy"
classifier.delete()
[training, testing]*.close()

def chart = new XYChartBuilder().width(900).height(450).title("Species Predicted[/Actual]").
        xAxisTitle("Petal length").yAxisTitle("Petal width").build()
petalL.keySet().each {
    chart.addSeries(it, petalW[it] as double[], petalL[it] as double[]).with {
        XYSeriesRenderStyle = Scatter
    }
}
new SwingWrapper(chart).displayChart()
