import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealMatrixChangingVisitor

import static org.apache.commons.math3.linear.MatrixUtils.createColumnRealMatrix

// inspired from https://github.com/ralscha/blog/tree/master/mnist
class Mnist {

	private static RealMatrix inputHidden
	private static RealMatrix hiddenOutput

	static int maxIndex(RealMatrix result) {
		double[][] data = result.data
		(0..<data.size()).max{ data[it][0] }
	}

	static load(klass) {
		def weights = klass.classLoader.getResourceAsStream('weights')
		try (ObjectInputStream ois = new ObjectInputStream(weights)) {
			double[][] weightsInputHidden = (double[][]) ois.readObject()
			double[][] weightsHiddenOutput = (double[][]) ois.readObject()
			inputHidden = MatrixUtils.createRealMatrix(weightsInputHidden)
			hiddenOutput = MatrixUtils.createRealMatrix(weightsHiddenOutput)
		}
	}

	static RealMatrix query(double[] inputArray) {
		RealMatrix inputs = createColumnRealMatrix(inputArray)
		RealMatrix hiddenInputs = inputHidden.multiply(inputs)
		RealMatrix hiddenOutputs = scalarSigmoid(hiddenInputs)
		RealMatrix finalInputs = hiddenOutput.multiply(hiddenOutputs)
		return scalarSigmoid(finalInputs)
	}

	static double[] scale(int[] img) {
		double[] result = new double[img.length]
		for (int i = 0; i < img.length; i++) {
			int red = (img[i] >> 16) & 0xff
			int green = (img[i] >> 8) & 0xff
			int blue = img[i] & 0xff
			result[i] = 1 - ((red + green + blue) / 765.0 * 999 + 1)/1000
		}
		return result
	}

	private static RealMatrix scalarSigmoid(RealMatrix matrix) {
		def sigmoid = [start: { a, b, c, d, e, f -> },
					   visit: { r, c, v -> 1 / (1 + Math.exp(-v)) },
					     end: { -> 0d }] as RealMatrixChangingVisitor
		RealMatrix result = matrix.copy()
		result.walkInRowOrder(sigmoid)
		result
	}
}
