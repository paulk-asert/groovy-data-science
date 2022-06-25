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

import org.encog.ConsoleStatusReportable
import org.encog.Encog
import org.encog.ml.data.MLData
import org.encog.ml.data.versatile.VersatileMLDataSet
import org.encog.ml.data.versatile.columns.ColumnType
import org.encog.ml.data.versatile.sources.CSVDataSource
import org.encog.ml.model.EncogModel
import org.encog.util.csv.CSVFormat
import org.encog.util.csv.ReadCSV

import static org.encog.ml.factory.MLMethodFactory.TYPE_FEEDFORWARD
import static org.encog.util.simple.EncogUtility.calculateRegressionError

def cols = ['Sepal length', 'Sepal width', 'Petal length', 'Petal width']
def species = ['Iris-setosa', 'Iris-versicolor', 'Iris-virginica']

def file = getClass().classLoader.getResource('iris_data.csv').file as File
def source = new CSVDataSource(file, true, CSVFormat.DECIMAL_POINT)
def data = new VersatileMLDataSet(source)
cols.eachWithIndex{ String col, int idx -> data.defineSourceColumn(col, idx, ColumnType.continuous) }
def outputColumn = data.defineSourceColumn('Species', 4, ColumnType.nominal)

data.analyze()
data.defineSingleOutputOthersInput(outputColumn)

// Create feedforward neural network as the model type.
// Other types:
// SVM:  Support Vector Machine (SVM)
// TYPE_RBFNETWORK: RBF Neural Network
// TYPE_NEAT: NEAT Neural Network
// TYPE_PNN: Probabilistic Neural Network
EncogModel model = new EncogModel(data).tap {
    selectMethod(data, TYPE_FEEDFORWARD)
    report = new ConsoleStatusReportable()
    data.normalize()
    holdBackValidation(0.3, true, 1001) // test with 30%
    selectTrainingType(data)
}

def bestMethod = model.crossvalidate(5, true) // 5-fold cross-validation

println "Training error: " + calculateRegressionError(bestMethod, model.trainingDataset)
println "Validation error: " + calculateRegressionError(bestMethod, model.validationDataset)

def helper = data.normHelper
//println helper

println "Final model: " + bestMethod

println 'Rerunning on entire dataset ...'
ReadCSV csv = new ReadCSV(file, true, CSVFormat.DECIMAL_POINT)
MLData input = helper.allocateInputVector()

def matrix = species.collectEntries{ actual -> [actual, species.collectEntries { predicted -> [predicted, 0] }] }
while (csv.next()) {
    String[] line = (0..3).collect{ csv.get(it) }
    String actual = csv.get(4)
    helper.normalizeInputVector(line, input.data, false)
    MLData output = bestMethod.compute(input)
    String predicted = helper.denormalizeOutputVectorToString(output)[0]
    matrix[actual][predicted]++
    println "$line -> predicted: $predicted, correct: $actual"
}
println "${'Confusion matrix:'.padRight(20)}${species.collect{ sprintf '%20s', it }.join()}"
matrix.each{ k, v ->
    println "${k.padLeft(20)}${v.values().collect{ sprintf '%20d', it }.join()}"
}

Encog.instance.shutdown()
