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
