import weka.classifiers.Evaluation
import weka.classifiers.functions.Dl4jMlpClassifier
import weka.core.Instances
import weka.dl4j.*
import weka.dl4j.activations.*
import weka.dl4j.layers.*
import weka.dl4j.lossfunctions.LossMCXENT
import weka.dl4j.updater.Adam
import weka.dl4j.iterators.instance.ImageInstanceIterator

def base = 'D:/projects/wekaDeeplearning4j/' as File
def data = new Instances(new FileReader("$base/datasets/nominal/mnist.meta.minimal.arff"))
data.classIndex = data.numAttributes() - 1

// Populate the image iterator
def imgIter = new ImageInstanceIterator(imagesLocation: "$base/datasets/nominal/mnist-minimal" as File,
        height: 28, width: 28, numChannels: 1, trainBatchSize: 16,)

def clf = new Dl4jMlpClassifier(seed: 1, numEpochs: 10, instanceIterator: imgIter)

// set up first network layer: convolution layer, 8 3x3 filter
def convLayer1 = new ConvolutionLayer(kernelSize: [3, 3], stride: [1, 1],
        activationFunction: new ActivationReLU(), NOut: 8)

// First maxpooling layer, 2x2 filter
def poolLayer1 = new SubsamplingLayer(poolingType: PoolingType.MAX, kernelSize: [2, 2], stride: [1, 1])

// Second convolution layer, 8 3x3 filter
def convLayer2 = new ConvolutionLayer(kernelSize: [3, 3], stride: [1, 1],
        activationFunction: new ActivationReLU(), NOut: 8)

// Second maxpooling layer, 2x2 filter
def poolLayer2 = new SubsamplingLayer(poolingType: PoolingType.MAX, kernelSize: [2, 2], stride: [1, 1])

// Output layer with softmax activation
def outputLayer = new OutputLayer(activationFunction: new ActivationSoftmax(), lossFn: new LossMCXENT())

// Set up the network configuration
def nnc = new NeuralNetConfiguration(updater: new Adam())
clf.neuralNetConfiguration = nnc

// Set the layers
clf.layers = [convLayer1, poolLayer1, convLayer2, poolLayer2, outputLayer]

// Evaluate the network
def eval = new Evaluation(data)
int numFolds = 10
eval.crossValidateModel(clf, data, numFolds, new Random(1))

println "% Correct = " + eval.pctCorrect()
