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
import groovy.transform.stc.POJO
import org.apache.beam.sdk.transforms.Combine
import org.apache.beam.sdk.values.KV

@CompileStatic
@POJO
class Squash extends Combine.CombineFn<KV<Integer, Point>, Accum, Points> {
    int k, dims

    @Override
    Accum createAccumulator() {
        new Accum()
    }

    @Override
    Accum addInput(Accum mutableAccumulator, KV<Integer, Point> input) {
        mutableAccumulator.pts << input.value
        mutableAccumulator
    }

    @Override
    Accum mergeAccumulators(Iterable<Accum> accumulators) {
        Accum result = createAccumulator()
        accumulators.each {
            result.pts += it.pts
        }
        result
    }

    @Override
    Points extractOutput(Accum accumulator) {
        var pts = accumulator.pts
        if (k && dims) {
            while (pts.size() < k) {
                pts << Point.ofRandom(dims)
            }
        }
        new Points(pts)
    }

    static class Accum implements Serializable {
        List<Point> pts = []
    }
}
