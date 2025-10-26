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
import weka.core.converters.ArffLoader

WekaPackageManager.loadPackages(true)

def tabnet = getClass().classLoader.getResource('tabnet.groovy').file
def source = getClass().classLoader.getResource('bolts.arff').file as File
def loader = new ArffLoader(file: source)
def data = loader.dataSet
data.classIndex = data.numAttributes() - 1

def options = Utils.splitOptions(/-network "weka.classifiers.djl.networkgenerator.GroovyGenerator -G $tabnet"/)
def classifier = Utils.forName(AbstractClassifier, 'weka.classifiers.djl.DJLRegressor', options)

def rand = new Random(0)
def randData = new Instances(data)
randData.randomize(rand)
randData.stratify(3)
def train = randData.trainCV(3, 0)
def test = randData.testCV(3, 0)

classifier.buildClassifier(train)
def eval = new Evaluation(test)
eval.evaluateModel(classifier, test)
println eval.toSummaryString()
