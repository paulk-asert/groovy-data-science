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

record Point(double[] pts) implements Serializable {
    private static Random r = new Random()
    private static Closure<double[]> randomPoint = { dims ->
        (1..dims).collect { r.nextGaussian() + 2 } as double[]
    }

    static Point ofRandom(int dims) {
        new Point(randomPoint(dims))
    }

    String toString() {
        "Point[${pts.collect{ sprintf '%.2f', it }.join('. ')}]"
    }
}
