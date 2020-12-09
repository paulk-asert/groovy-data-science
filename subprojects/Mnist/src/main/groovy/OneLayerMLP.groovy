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
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.learning.config.Nesterovs
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction
import org.slf4j.LoggerFactory

/*
 * A Multi Layered Perceptron (MLP) with one input layer and one hidden layer,
 * applied to the digit classification task of the MNIST Dataset
 *
 * Based on the corresponding example from DL4J:
 * github.com/deeplearning4j/dl4j-examples/blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/feedforward/mnist/MLPMnistSingleLayerExample.java
 */
int rngSeed = 123
int batchSize = 125
int numEpochs = 15
int numInputs = 28 * 28
int hiddenLayerSize = 1000
int numOutputs = 10

def (trainSet, testSet) = [true, false].collect { new MnistDataSetIterator(batchSize, it, rngSeed) }
def log = LoggerFactory.getLogger(getClass())
def conf = new NeuralNetConfiguration.Builder()
        .seed(rngSeed) //include the random seed for reproducibility
        // use stochastic gradient descent as an optimization algorithm
        .updater(new Nesterovs(0.006, 0.9))
        .l2(1e-4)
        .list()
        // input layer:
        .layer(new DenseLayer.Builder()
                .nIn(numInputs)
                .nOut(hiddenLayerSize)
                .activation(Activation.RELU)
                .weightInit(WeightInit.XAVIER)
                .build())
        // hidden layer:
        .layer(new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
                .nIn(hiddenLayerSize)
                .nOut(numOutputs)
                .activation(Activation.SOFTMAX)
                .weightInit(WeightInit.XAVIER)
                .build())
        .build()

log.info("Creating model ...")
def model = new MultiLayerNetwork(conf)
model.init()

log.info("Training model ...")
model.listeners = new ScoreIterationListener(100)
model.fit(trainSet, numEpochs)
//model.save('mlp1_model.dat' as File)

log.info("Evaluating model...")
def eval = model.evaluate(testSet)
log.info(eval.stats())
