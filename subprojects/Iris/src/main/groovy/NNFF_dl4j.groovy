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

// modelled on:
// https://github.com/eclipse/deeplearning4j-examples/blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/quickstart/modeling/feedforward/classification/IrisClassifier.java

import org.datavec.api.records.reader.impl.csv.CSVRecordReader
import org.datavec.api.split.FileSplit
import org.datavec.api.writable.Writable
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.evaluation.classification.Evaluation
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize
import org.nd4j.linalg.learning.config.Sgd
import org.nd4j.linalg.lossfunctions.LossFunctions

def species = ['Iris-setosa', 'Iris-versicolor', 'Iris-virginica']

int numLinesToSkip = 1
// hand-crafted record reader to avoid defining schema and spark-based transform
// RecordReaderDataSetIterator expects all numbers so convert last column
// Columns: Sepal length,Sepal width,Petal length,Petal width,Class
def recordReader = new CSVRecordReader(numLinesToSkip) {
    protected List<Writable> parseLine(String line) {
        for (i in 0..<species.size()) {
            if (line.endsWith(species[i])) {
                line = line - species[i] + i
                break
            }
        }
        super.parseLine(line)
    }
}

def file = getClass().classLoader.getResource('iris_data.csv').file as File
recordReader.initialize(new FileSplit(file))

int labelIndex = 4     // Class
int numClasses = species.size()
int batchSize = 150

def iterator = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, numClasses)
DataSet allData = iterator.next()
allData.shuffle()
def testAndTrain = allData.splitTestAndTrain(0.8)

DataSet trainingData = testAndTrain.train
DataSet testData = testAndTrain.test

// scale all data to be between -1 .. 1
def scaler = new NormalizerStandardize()
scaler.fit(trainingData)
scaler.transform(trainingData)
scaler.transform(testData)

int numInputs = 4
int outputNum = 3
long seed = -1

println "Build model...."
MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
        .seed(seed)
        .activation(Activation.TANH) // global activation
        .weightInit(WeightInit.XAVIER)
        .updater(new Sgd(0.1))
        .l2(1e-4)
        .list()
        .layer(new DenseLayer.Builder().nIn(numInputs).nOut(3).build())
        .layer(new DenseLayer.Builder().nIn(3).nOut(3).build())
        .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .activation(Activation.SOFTMAX) // override global activation with softmax for this layer
                .nIn(3).nOut(outputNum).build())
        .build()

def model = new MultiLayerNetwork(conf)
model.init()

model.listeners = new ScoreIterationListener(100)

1000.times {model.fit(trainingData) }

def eval = new Evaluation(3)
def output = model.output(testData.features)
eval.eval(testData.labels, output)
println eval.stats()
