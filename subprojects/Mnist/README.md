# Mnist digit recognition

Groovy code examples can be found in the [src](src/main/groovy) folder.
If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

## Deeplearning4j example

A multilayer perceptron (MLP) is a class of feed-forward artificial neural network, i.e. each
layer feeds forward to the next layer based on weights and thresholds.

An MLP consists of at least three layers of nodes: an input layer, a hidden layer and an output layer.
Additional layers increase model accuracy or power at the cost of additional computation time.
Except for the input nodes, each node is a neuron that uses a non-linear activation function.
MLP uses a supervised learning technique known as back-propagation for training.

The [OneLayerMLP](src/main/groovy/OneLayerMLP.groovy)
and [TwoLayerMLP](src/main/groovy/TwoLayerMLP.groovy) examples use
[Deep Learning4J](https://deeplearning4j.org/).

## Apache Commons Math Matrix example

The scripts for this example use
[Apache Commons Math](https://commons.apache.org/proper/commons-math/)
for matrix calculations.

The [Gui](src/main/groovy/Gui.groovy) application reads a pre-saved model and uses that to predict digits "written" onto the GUI.

__Requirements__: The `Gui` example runs only on JDK 8 since it is using a JDK 8 only version of GroovyFX.

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
