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
import deepnetts.data.TabularDataSet
import deepnetts.eval.ClassifierEvaluator
import deepnetts.net.FeedForwardNetwork
import deepnetts.net.layers.activation.ActivationType
import deepnetts.net.loss.LossType
import deepnetts.net.train.opt.OptimizerType
import static deepnetts.data.DataSets.*

// inspired by:
// https://github.com/deepnetts/deepnetts-communityedition/blob/community-visrec/deepnetts-examples/src/main/java/deepnetts/examples/IrisFlowersClassifier.java

String[] cols = ['Sepal length', 'Sepal width', 'Petal length', 'Petal width']
String[] species = ['Iris-setosa', 'Iris-versicolor', 'Iris-virginica']

int numInputs = cols.size()
int numOutputs = species.size()

// Deep Netts readCsv assumes normalized data, so we roll our own
var dataSet = new TabularDataSet(numInputs, numOutputs).tap{ columnNames = cols + species }
var data = new File('iris_data.csv').readLines()*.split(',')
data[1..-1].each {
    dataSet.add(new TabularDataSet.Item(it[0..3]*.toFloat() as float[], oneHotEncode(it[4], species)))
}
scaleMax(dataSet)

var splits = dataSet.split(0.7d, 0.3d)  // 70/30% split
var train = splits[0]
var test = splits[1]

var neuralNet = FeedForwardNetwork.builder()
    .addInputLayer(numInputs)
    .addFullyConnectedLayer(5, ActivationType.TANH)
    .addOutputLayer(numOutputs, ActivationType.SOFTMAX)
    .lossFunction(LossType.CROSS_ENTROPY)
    .randomSeed(543)
    .build()

neuralNet.trainer.with {
    maxError = 0.1f
    learningRate = 0.01f
    momentum = 0.9f
    optimizer = OptimizerType.MOMENTUM
}

neuralNet.train(train)

new ClassifierEvaluator().with {
    println "CLASSIFIER EVALUATION METRICS\n${evaluate(neuralNet, test)}"
    println "CONFUSION MATRIX\n$confusionMatrix"
}
