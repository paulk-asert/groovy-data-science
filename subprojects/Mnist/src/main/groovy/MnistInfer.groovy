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
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix

import static Util.scalarSigmoid
import static org.apache.commons.math3.linear.MatrixUtils.createColumnRealMatrix

// inspired from https://github.com/ralscha/blog/tree/master/mnist
@CompileStatic
class MnistInfer {
    private static RealMatrix inputHidden
    private static RealMatrix hiddenOutput

    static void load(Class klass) {
        def weights = klass.classLoader.getResourceAsStream('weights')
        try (ObjectInputStream ois = new ObjectInputStream(weights)) {
            inputHidden = MatrixUtils.createRealMatrix((double[][]) ois.readObject())
            hiddenOutput = MatrixUtils.createRealMatrix((double[][]) ois.readObject())
        }
    }

    static RealMatrix query(double[] inputArray) {
        RealMatrix inputs = createColumnRealMatrix(inputArray)
        RealMatrix hiddenInputs = inputHidden * inputs
        RealMatrix hiddenOutputs = scalarSigmoid(hiddenInputs)
        RealMatrix finalInputs = hiddenOutput * hiddenOutputs
        return scalarSigmoid(finalInputs)
    }

    static double[] normalize(int[] img) {
        double[] result = new double[img.length]
        for (i in 0..<img.length) {
            int red = (img[i] >> 16) & 0xff
            int green = (img[i] >> 8) & 0xff
            int blue = img[i] & 0xff
            result[i] = 1 - ((red + green + blue) / 765.0 * 999 + 1) / 1000
        }
        return result
    }
}
