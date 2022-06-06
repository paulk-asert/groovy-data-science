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

import weka.classifiers.AbstractClassifier
import weka.classifiers.Evaluation
import weka.core.Instances
import weka.core.Utils
import weka.core.WekaPackageManager
import weka.core.converters.CSVLoader

WekaPackageManager.loadPackages(true)

def file = getClass().classLoader.getResource('iris_data.csv').file as File
def loader = new CSVLoader(file: file)
def data = loader.dataSet
data.classIndex = 4

def options = Utils.splitOptions("-S 1 -numEpochs 10 -layer \"weka.dl4j.layers.OutputLayer -activation weka.dl4j.activations.ActivationSoftmax -lossFn weka.dl4j.lossfunctions.LossMCXENT\"")
AbstractClassifier myClassifier = Utils.forName(AbstractClassifier, "weka.classifiers.functions.Dl4jMlpClassifier", options)

// Stratify and split
Random rand = new Random(0)
Instances randData = new Instances(data)
randData.randomize(rand)
randData.stratify(3)
Instances train = randData.trainCV(3, 0)
Instances test = randData.testCV(3, 0)

// Build the classifier on the training data
myClassifier.buildClassifier(train)

// Evaluate the model on test data
Evaluation eval = new Evaluation(test)
eval.evaluateModel(myClassifier, test)

println eval.toSummaryString()
println eval.toMatrixString()
