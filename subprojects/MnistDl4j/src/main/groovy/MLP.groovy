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

/** A Multi Layered Perceptron (MLP) with a single hidden layer,
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
        // hidden layer:
        .layer(0, new DenseLayer.Builder()
                .nIn(numInputs)
                .nOut(hiddenLayerSize)
                .activation(Activation.RELU)
                .weightInit(WeightInit.XAVIER)
                .build())
        // output layer:
        .layer(1, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
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

log.info("Evaluating model...")
def eval = model.evaluate(testSet)
log.info(eval.stats())
