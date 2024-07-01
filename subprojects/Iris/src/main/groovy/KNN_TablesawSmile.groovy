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

import smile.classification.KNN
import smile.validation.metric.ConfusionMatrix
import smile.validation.CrossValidation
import tech.tablesaw.api.StringColumn
import tech.tablesaw.api.Table
import tech.tablesaw.plotly.api.ScatterPlot

import static tech.tablesaw.aggregate.AggregateFunctions.*

def features = ['Sepal length', 'Sepal width', 'Petal length', 'Petal width']
def species = ['Iris-setosa', 'Iris-versicolor', 'Iris-virginica']
def file = getClass().classLoader.getResource('iris_data.csv').file
Table table = Table.read().csv(file)
def helper = new TablesawUtil(file)

println """
Shape: ${table.shape()}

${table.structure()}

Frequency analysis:
${table.xTabCounts('Class')}

Feature stats by species:"""

(0..<features.size()).each {
    println table.summarize(features[it], mean, min, max).by('Class')
}

def dataFrame = table.smile().toDataFrame()

def featureCols = dataFrame.drop('Class').toArray()
def classNames = dataFrame.column('Class').toStringArray()
int[] classes = classNames.collect{species.indexOf(it) }

// train and predict on complete data set to show graph
def knn = KNN.fit(featureCols, classes, 3)
def predictions = knn.predict(featureCols)
println """
Confusion matrix:
${ConfusionMatrix.of(classes, predictions)}
"""

table = table.addColumns(StringColumn.create('Result', predictions.indexed().collect{ idx, predictedClass ->
    def (actual, predicted) = [classNames[idx], species[predictedClass]]
    actual == predicted ? predicted : "$predicted/$actual".toString() }))

def title = 'Petal width vs length with predicted[/actual] class'
helper.show(ScatterPlot.create(title, table, 'Petal width', 'Petal length', 'Result'), 'KNNClassification_petal')
title = 'Sepal width vs length with predicted[/actual] class'
helper.show(ScatterPlot.create(title, table, 'Sepal width', 'Sepal length', 'Result'), 'KNNClassification_sepal')

// use cross validation to get accuracy
CrossValidation.classification(10, featureCols, classes, (x, y) -> KNN.fit(x, y, 3)).with {
    printf 'Accuracy: %.2f%% +/- %.2f\n', 100 * avg.accuracy, 100 * sd.accuracy
}
