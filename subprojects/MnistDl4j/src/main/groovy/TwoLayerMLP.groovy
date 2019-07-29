import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.learning.config.Nadam
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction
import org.slf4j.LoggerFactory

/*
 * A Multi Layered Perceptron (MLP) with two input layers and one hidden layer,
 * applied to the digit classification task of the MNIST Dataset
 *
 * Based on the corresponding example from DL4J:
 * github.com/deeplearning4j/dl4j-examples/blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/feedforward/mnist/MLPMnistTwoLayerExample.java
 */
int rngSeed = 123
int batchSize = 64
int numEpochs = 15
int numInputs = 28 * 28
int numOutputs = 10
double rate = 0.0015

def (trainSet, testSet) = [true, false].collect { new MnistDataSetIterator(batchSize, it, rngSeed) }
def log = LoggerFactory.getLogger(getClass())
def conf = new NeuralNetConfiguration.Builder()
        .seed(rngSeed) //include the random seed for reproducibility
        // use stochastic gradient descent as an optimization algorithm
        .updater(new Nadam())
        .l2(rate * 0.005) // regularized
        .list()
        // first input layer:
        .layer(new DenseLayer.Builder()
                .nIn(numInputs)
                .nOut(500)
                .build())
        // second input layer:
        .layer(new DenseLayer.Builder()
                .nIn(500)
                .nOut(100)
                .build())
        // hidden layer:
        .layer(new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
                .nOut(numOutputs)
                .activation(Activation.SOFTMAX)
                .build())
        .build()

log.info("Creating model ...")
def model = new MultiLayerNetwork(conf)
model.init()

log.info("Training model ...")
model.listeners = new ScoreIterationListener(100)
model.fit(trainSet, numEpochs)

log.info("Evaluating model...")
def eval = model.evaluate(testSet)
log.info(eval.stats())
