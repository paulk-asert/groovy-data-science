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
import smile.validation.ConfusionMatrix
import smile.validation.CrossValidation
import tech.tablesaw.api.StringColumn
import tech.tablesaw.api.Table
import tech.tablesaw.plotly.api.ScatterPlot

import static tech.tablesaw.aggregate.AggregateFunctions.*

def cols = ['Sepal length', 'Sepal width', 'Petal length', 'Petal width']
def species = ['Iris-setosa', 'Iris-versicolor', 'Iris-virginica']
def file = getClass().classLoader.getResource('iris_data.csv').file
Table rows = Table.read().csv(file)
def helper = new TablesawUtil(file)

println rows.shape()

println rows.structure()
println rows.xTabCounts('Class')
(0..3).each {
    println rows.summarize(cols[it], mean, min, max).by('Class')
}

def iris = rows.smile().toDataFrame()

def features = iris.drop("Class").toArray()
def classes = iris.column("Class").toStringArray()
int[] classIndexs = classes.collect{species.indexOf(it) }
def predictions = CrossValidation.classification(10, features, classIndexs, (x, y) -> KNN.fit(x, y, 3))
rows = rows.addColumns(StringColumn.create('Result', predictions.indexed().collect{ idx, predictedClass ->
    def (actual, predicted) = [classes[idx], species[predictedClass]]
    actual == predicted ? predicted : "$predicted/$actual".toString() }))
println ConfusionMatrix.of(classIndexs, predictions)

def title = 'Petal width vs length with predicted[/actual] class'
helper.show(ScatterPlot.create(title, rows, 'Petal width', 'Petal length', 'Result'), 'KNNClassification')
