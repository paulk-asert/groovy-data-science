# Mnist digit recognition

An artificial neural network consists of layers of nodes which talk to other nodes based on weights and thresholds.
It mimics to some degree the way in which neurons are interconnected in biological neural networks.
Neural networks are particularly good when some degree of fuzziness is required in the processing activity.
They are popular for speech and image recognition and medical diagnosis among other areas.
This example looks at using neural networks for digit recognition.

![Running Gui.groovy](../../docs/images/mnist_gui.png)

Groovy code examples can be found in the [src/main/groovy](src/main/groovy) folder.
The examples illustrate:
* hand-written neural networks with
[Apache Commons Math](https://commons.apache.org/proper/commons-math/) used for matrix calculations
* multilayer perceptron examples using
[Eclipse DeepLearning4J](https://deeplearning4j.konduit.ai/)

If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

## Apache Commons Math Matrix example

The scripts for this example use
[Apache Commons Math](https://commons.apache.org/proper/commons-math/)
for matrix calculations.

The [Gui](src/main/groovy/Gui.groovy) application reads a pre-saved model and uses that to predict digits "written" onto the GUI.

## Deeplearning4j example

A multilayer perceptron (MLP) is a class of feed-forward artificial neural network, i.e. each
layer feeds forward to the next layer based on weights and thresholds.

An MLP consists of at least three layers of nodes: an input layer, a hidden layer and an output layer.
Additional layers increase model accuracy or power at the cost of additional computation time.
Except for the input nodes, each node is a neuron that uses a non-linear activation function.
MLP uses a supervised learning technique known as back-propagation for training.

The [OneLayerMLP](src/main/groovy/OneLayerMLP.groovy)
and [TwoLayerMLP](src/main/groovy/TwoLayerMLP.groovy) examples use
[Eclipse DeepLearning4J](https://deeplearning4j.konduit.ai/).

The [MnistTrainer](src/main/groovy/MnistTrainer.groovy) script reads the MNIST dataset and trains and saves the neural network.
You only need to run this if you want to tweak or re-generate the model.

__Data details__:
The MNIST dataset is freely available from:<br>
http://yann.lecun.com/exdb/mnist/

The trainer script uses these 4 files:

train-images-idx3-ubyte.gz<br>
train-labels-idx1-ubyte.gz<br>
t10k-images-idx3-ubyte.gz<br>
t10k-labels-idx1-ubyte.gz

You should update the path to these files in the script before running.

The `train*` files are the training dataset (60,000 images).<br>
The `t10k*` files are the test dataset (10,000 images).<br>
The `*images*` files contain the pixel data of the images.<br>
The `*label*` files contain the label (number 0-9) for each image.

Since the trainer script is primarily interested in training the model,
the `t10k` files aren't strictly necessary.
Currently the script outputs the model accuracy before saving.
You could avoid downloading the `t10k` files if
you delete the parts of the code which check accuracy.

__Requirements__:

* The `Gui` example is using a JDK 8 only version of GroovyFX.
  It requires JDK 8 with JavaFX, e.g. Oracle JDK8 or Zulu JDK8 bundled with JavaFX
* Other examples have been tested on JDK8, JDK11 and JDK17.
