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

import org.apache.wayang.core.api.WayangContext
import org.apache.wayang.java.Java
import org.apache.wayang.ml4all.abstraction.api.LocalStage
import org.apache.wayang.ml4all.abstraction.api.Transform
import org.apache.wayang.ml4all.abstraction.plan.ML4allModel
import org.apache.wayang.ml4all.abstraction.plan.ML4allPlan
import org.apache.wayang.ml4all.algorithms.kmeans.KMeansCompute
import org.apache.wayang.ml4all.algorithms.kmeans.KMeansConvergeOrMaxIterationsLoop
import org.apache.wayang.ml4all.algorithms.kmeans.KMeansUpdate
//import org.apache.wayang.ml4all.algorithms.kmeans.TransformCSV
import org.apache.wayang.spark.Spark

int k = 3
int maxIterations = 100
double accuracy = 0

class TransformCSV extends Transform<double[], String> {
    double[] transform(String input) {
        input.split(',')[2..-1] as double[]
    }
}

class KMeansStageWithRandoms extends LocalStage {
    int k, dimension
    private r = new Random()

    void staging(ML4allModel model) {
        double[][] centers = new double[k][]
        for (i in 0..<k) {
            centers[i] = (0..<dimension).collect { r.nextGaussian() + 2 } as double[]
        }
        model.put('centers', centers)
    }
}

var url = WhiskeyWayangML.classLoader.getResource('whiskey_noheader.csv').path
var dims = 12
var context = new WayangContext()
    .withPlugin(Spark.basicPlugin())
    .withPlugin(Java.basicPlugin())

var plan = new ML4allPlan(
    transformOp: new TransformCSV(),
    localStage: new KMeansStageWithRandoms(k: k, dimension: dims),
    computeOp: new KMeansCompute(),
    updateOp: new KMeansUpdate(),
    loopOp: new KMeansConvergeOrMaxIterationsLoop(accuracy, maxIterations)
)

var model = plan.execute('file:' + url, context)
model.getByKey("centers").eachWithIndex { center, idx ->
    var pts = center.collect('%.2f'::formatted).join(', ')
    println "Cluster$idx: $pts"
}

/*
Cluster0: 1.57, 2.32, 1.32, 0.45, 0.09, 1.08, 1.19, 0.60, 1.26, 1.74, 1.72, 1.85
Cluster1: 3.43, 1.57, 3.43, 3.14, 0.57, 0.14, 1.71, 0.43, 1.29, 1.43, 1.29, 0.14
Cluster2: 2.73, 2.42, 1.46, 0.04, 0.04, 1.88, 1.69, 1.88, 1.92, 2.04, 2.12, 1.81
*/
