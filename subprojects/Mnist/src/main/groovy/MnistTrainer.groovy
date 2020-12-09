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
import groovy.transform.CompileStatic
import org.apache.commons.math3.linear.RealMatrix
import java.nio.file.Paths

import static org.apache.commons.math3.linear.MatrixUtils.createColumnRealMatrix
import static org.apache.commons.math3.linear.MatrixUtils.createRealMatrix
import static Util.*

@CompileStatic
class MnistNeuralNet {
    private static int inodes = 784
    private static int hnodes = 200
    private static int onodes = 10
    private static double learning_rate = 0.1
    private static RealMatrix inputHidden
    private static RealMatrix hiddenOutput

    static initRandom() {
        inputHidden = initRandom(createRealMatrix(hnodes, inodes), inodes**-0.5 as double)
        hiddenOutput = initRandom(createRealMatrix(onodes, hnodes), hnodes**-0.5 as double)
    }

    static double[] normalize(Integer[] img) {
        img.collect { it / 255.0d * 0.999d + 0.001d } as double[]
    }

    static RealMatrix createTarget(int label) {
        createRealMatrix(onodes, 1).tap {
            (0..<onodes).each { setEntry(it, 0, it != label ? 0.001d : 0.999d) }
        }
    }

    static RealMatrix query(double[] inputArray) {
        RealMatrix inputs = createColumnRealMatrix(inputArray)
        RealMatrix hiddenInputs = inputHidden * inputs
        RealMatrix hiddenOutputs = scalarSigmoid(hiddenInputs)
        RealMatrix finalInputs = hiddenOutput * hiddenOutputs
        return scalarSigmoid(finalInputs)
    }

    static RealMatrix initRandom(RealMatrix matrix, double desiredStandardDeviation) {
        scalar(matrix, it -> new Random().nextGaussian() * desiredStandardDeviation)
    }

    static void train(RealMatrix inputs, RealMatrix targets) {
        // forward
        RealMatrix hiddenInputs = inputHidden * inputs
        RealMatrix hiddenOutputs = scalarSigmoid(hiddenInputs)
        RealMatrix finalInputs = hiddenOutput * hiddenOutputs
        RealMatrix finalOutputs = scalarSigmoid(finalInputs)

        // back
        RealMatrix outputErrors = targets.subtract(finalOutputs)
        RealMatrix t1 = multiplyElements(outputErrors, finalOutputs)
        RealMatrix t2 = multiplyElements(t1, scalar(finalOutputs, it -> 1.0d - it))
        RealMatrix t3 = t2 * hiddenOutputs.transpose()
        hiddenOutput = hiddenOutput.add(scalar(t3, it -> learning_rate * it))

        RealMatrix hiddenErrors = hiddenOutput.transpose() * outputErrors
        t1 = multiplyElements(hiddenErrors, hiddenOutputs)
        t2 = multiplyElements(t1, scalar(hiddenOutputs, it -> 1.0d - it))
        t3 = t2 * inputs.transpose()
        inputHidden = inputHidden.add(scalar(t3, it -> learning_rate * it))
    }

    static void save(String filename) {
        new FileOutputStream(filename).withObjectOutputStream { oos ->
            oos.writeObject(inputHidden.data)
            oos.writeObject(hiddenOutput.data)
        }
    }
}

import static MnistNeuralNet.*

int epochs = 10
initRandom()

int[] labels = MnistReader.getLabels(Paths.get("/path/to/train-labels-idx1-ubyte.gz"))
List<int[][]> images = MnistReader.getImages(Paths.get("/path/to/train-images-idx3-ubyte.gz"))

println 'Processing orig'
def scaledImages = images.collect { image -> normalize(image.flatten() as Integer[]) } as double[][]
println 'Processing +10°'
def scaledImagesRot10 = images.collect { image -> normalize(rotate(image, 10).flatten() as Integer[]) } as double[][]
println 'Processing -10°'
def scaledImagesRot350 = images.collect { image -> normalize(rotate(image, -10).flatten() as Integer[]) } as double[][]

epochs.times { e ->
    println "Running epoch: ${e + 1}"
    for (i in 0..<labels.length) {
        RealMatrix targets = createTarget(labels[i])
        train(createColumnRealMatrix(scaledImages[i]), targets)
        train(createColumnRealMatrix(scaledImagesRot10[i]), targets)
        train(createColumnRealMatrix(scaledImagesRot350[i]), targets)
    }
}

int[] testLabels = MnistReader.getLabels(Paths.get("/path/to/t10k-labels-idx1-ubyte.gz"))
List<int[][]> testImages = MnistReader.getImages(Paths.get("/path/to/t10k-images-idx3-ubyte.gz"))

int correct = 0
for (i in 0..<testLabels.length) {
    int correctLabel = testLabels[i]
    RealMatrix predict = query(normalize(testImages.get(i).flatten() as Integer[]))
    int predictLabel = maxIndex(predict)
    if (predictLabel == correctLabel) correct++
}

println "Accuracy: " + correct / testLabels.length
save("../resources/weights")
