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

// inspired by the Apache Wayang Scala example here:
// https://github.com/apache/incubator-wayang/blob/main/README.md#k-means

import org.apache.wayang.api.JavaPlanBuilder
import org.apache.wayang.core.api.WayangContext
import org.apache.wayang.core.function.ExecutionContext
import org.apache.wayang.core.function.FunctionDescriptor.ExtendedSerializableFunction
import org.apache.wayang.core.function.FunctionDescriptor.SerializableBinaryOperator
import org.apache.wayang.core.function.FunctionDescriptor.SerializableFunction
import org.apache.wayang.java.Java
import org.apache.wayang.spark.Spark
import static java.lang.Math.sqrt

record Point(double[] pts) implements Serializable {
    static Point fromLine(String line) {
        new Point(line.split(',')[2..-1] as double[]) }
}

record PointGrouping(double[] pts, int cluster, long count) implements Serializable {
    PointGrouping(List<Double> pts, int cluster, long count) {
        this(pts as double[], cluster, count)
    }

    PointGrouping plus(PointGrouping that) {
        var newPts = pts.indices.collect{ pts[it] + that.pts[it] }
        new PointGrouping(newPts, cluster, count + that.count)
    }

    PointGrouping average() {
        new PointGrouping(pts.collect{ double d -> d/count }, cluster, count)
    }
}

class SelectNearestCentroid implements ExtendedSerializableFunction<Point, PointGrouping> {
    Iterable<PointGrouping> centroids

    void open(ExecutionContext context) {
        centroids = context.getBroadcast('centroids')
    }

    PointGrouping apply(Point p) {
        var minDistance = Double.POSITIVE_INFINITY
        var nearestCentroidId = -1
        for (c in centroids) {
            var distance = sqrt(p.pts.indices.collect{ p.pts[it] - c.pts[it] }.sum{ it ** 2 } as double)
            if (distance < minDistance) {
                minDistance = distance
                nearestCentroidId = c.cluster
            }
        }
        new PointGrouping(p.pts, nearestCentroidId, 1)
    }
}

class PipelineOps {
    public static SerializableFunction<PointGrouping, Integer> cluster = tpc -> tpc.cluster
    public static SerializableFunction<PointGrouping, PointGrouping> average = tpc -> tpc.average()
    public static SerializableBinaryOperator<PointGrouping> plus = (tpc1, tpc2) -> tpc1 + tpc2
}
import static PipelineOps.*

int k = 5
int iterations = 10

// read in data from our file
var url = WhiskeyWayang.classLoader.getResource('whiskey.csv').file
var pointsData = new File(url).readLines()[1..-1].collect{ Point.fromLine(it) }
var dims = pointsData[0].pts.size()

// create some random points as initial centroids
var r = new Random()
var randomPoint = { (0..<dims).collect { r.nextGaussian() + 2 } as double[] }
var initPts = (1..k).collect(randomPoint)

var context = new WayangContext()
    .withPlugin(Java.basicPlugin())
    .withPlugin(Spark.basicPlugin())
var planBuilder = new JavaPlanBuilder(context, "KMeans ($url, k=$k, iterations=$iterations)")

var points = planBuilder
    .loadCollection(pointsData).withName('Load points')

var initialCentroids = planBuilder
    .loadCollection((0..<k).collect{ idx -> new PointGrouping(initPts[idx], idx, 0) })
    .withName('Load random centroids')

var finalCentroids = initialCentroids.repeat(iterations, currentCentroids ->
    points.map(new SelectNearestCentroid())
        .withBroadcast(currentCentroids, 'centroids').withName('Find nearest centroid')
        .reduceByKey(cluster, plus).withName('Aggregate points')
        .map(average).withName('Average points')
        .withOutputClass(PointGrouping)
).withName('Loop')

println 'Centroids:'
finalCentroids.forEach { c ->
    var pts = c.pts.collect { sprintf '%.2f', it }.join(', ')
    println "Cluster$c.cluster ($c.count points): $pts"
}
/*
Centroids:
Cluster0 (20 points): 2.00, 2.50, 1.55, 0.35, 0.20, 1.15, 1.55, 0.95, 0.90, 1.80, 1.35, 1.35
Cluster2 (21 points): 2.81, 2.43, 1.52, 0.05, 0.00, 1.90, 1.67, 2.05, 2.10, 2.10, 2.19, 1.76
Cluster3 (34 points): 1.38, 2.32, 1.09, 0.26, 0.03, 1.15, 1.09, 0.47, 1.38, 1.74, 2.03, 2.24
Cluster4 (11 points): 2.91, 1.55, 2.91, 2.73, 0.45, 0.45, 1.45, 0.55, 1.55, 1.45, 1.18, 0.55
*/
