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
import org.apache.beam.sdk.transforms.SerializableFunction

import static java.lang.Math.sqrt

class AggregateModelStats implements SerializableFunction<Iterable<double[]>, double[]> {
    @Override
    double[] apply(Iterable<double[]> input) {
        double[] sum = null
        for (double[] next : input) {
            if (sum == null) {
                sum = new double[next.size()]
                (0..<sum.size()).each { sum[it] = 0.0d }
            }
            def total = sum[2] + next[2]
            sum[0] = sqrt((sum[2] * sum[0] * sum[0] + next[2] * next[0] * next[0]) / total)
            sum[1] = (sum[2] * sum[1] + next[2] * next[1]) / total
            sum[2] = total
        }
        return sum
    }
}
