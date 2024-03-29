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
import org.apache.commons.math3.linear.RealMatrixChangingVisitor

import java.util.function.Function

import static org.apache.commons.math3.linear.MatrixUtils.checkAdditionCompatible
import static org.apache.commons.math3.linear.MatrixUtils.createRealMatrix

@CompileStatic
class Util {
    private Util() {}

    static displayResult(List<Double> items, Integer predict) {
        items.indexed().collect { idx, val ->
            def marker = (idx == predict && items[predict] > 0.5) ? ' **' : ''
            "$idx ${val ? sprintf('%.4f', val) + marker : '?'}"
        }.join('\n')
    }

    static RealMatrix scalar(RealMatrix matrix, Function<Double, Double> function) {
        int numRows = matrix.rowDimension
        int numCols = matrix.columnDimension
        RealMatrix result = createRealMatrix(numRows, numCols)
        for (r in 0..<numRows) {
            for (c in 0..<numCols) {
                result.setEntry(r, c, function.apply(matrix.getEntry(r, c)))
            }
        }
        return result
    }

    static int[][] rotate(int[][] img, double angleInDegrees) {
        double angle = Math.toRadians(angleInDegrees)
        int[][] result = new int[img.length][]
        for (y in 0..<img.length) {
            result[y] = new int[img[y].length]
            Arrays.fill(result[y], 0)
        }

        double cosAngle = Math.cos(angle)
        double sinAngle = Math.sin(angle)
        double x0 = img[0].length / 2 - cosAngle * img[0].length / 2 - sinAngle * img.length / 2
        double y0 = img.length / 2 - cosAngle * img.length / 2 + sinAngle * img[0].length / 2

        for (y in 0..<img.length) {
            for (x in 0..<img[y].length) {
                int xRot = (int) (x * cosAngle + y * sinAngle + x0)
                int yRot = (int) (-x * sinAngle + y * cosAngle + y0)
                if (xRot >= 0 && yRot >= 0 && xRot <= 27 && yRot <= 27) {
                    result[y][x] = img[yRot][xRot]
                }
            }
        }
        return result
    }

    static RealMatrix multiplyElements(RealMatrix matrixA, RealMatrix matrixB) {
        // elementWise multiplication has same compatibility requirements as addition
        checkAdditionCompatible(matrixA, matrixB)
        int numRows = matrixA.rowDimension
        int numCols = matrixA.columnDimension
        RealMatrix product = createRealMatrix(numRows, numCols)
        for (r in 0..<numRows) {
            for (c in 0..<numCols) {
                product.setEntry(r, c, matrixA.getEntry(r, c) * matrixB.getEntry(r, c))
            }
        }
        return product
    }

    static int maxIndex(RealMatrix result) {
        double[][] data = result.data
        (0..<data.size()).max { data[it][0] }
    }

    static RealMatrix scalarSigmoid(RealMatrix matrix) {
        def sigmoid = [start: { a, b, c, d, e, f -> },
                       visit: { r, c, double v -> 1 / (1 + Math.exp(-v)) },
                       end  : { -> 0d }] as RealMatrixChangingVisitor
        RealMatrix result = matrix.copy()
        result.walkInRowOrder(sigmoid)
        result
    }
}
