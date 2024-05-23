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
import org.apache.wayang.core.api.Configuration
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

record TaggedPointCounter(double[] pts, int cluster, long count) implements Serializable {
    TaggedPointCounter(List<Double> pts, int cluster, long count) {
        this(pts as double[], cluster, count)
    }

    TaggedPointCounter plus(TaggedPointCounter that) {
        var newPts = pts.indices.collect{ pts[it] + that.pts[it] }
        new TaggedPointCounter(newPts, cluster, count + that.count)
    }

    TaggedPointCounter average() {
        new TaggedPointCounter(pts.collect{ double d -> d/count }, cluster, count)
    }
}

class SelectNearestCentroid implements
        ExtendedSerializableFunction<Point, TaggedPointCounter> {
    Iterable<TaggedPointCounter> centroids

    void open(ExecutionContext context) {
        centroids = context.getBroadcast("centroids")
    }

    TaggedPointCounter apply(Point p) {
        var minDistance = Double.POSITIVE_INFINITY
        var nearestCentroidId = -1
        for (c in centroids) {
            var distance = sqrt((0..<p.pts.size()).collect{ p.pts[it] - c.pts[it] }.sum{ it ** 2 } as double)
            if (distance < minDistance) {
                minDistance = distance
                nearestCentroidId = c.cluster
            }
        }
        new TaggedPointCounter(p.pts, nearestCentroidId, 1)
    }
}

class Cluster implements SerializableFunction<TaggedPointCounter, Integer> {
    Integer apply(TaggedPointCounter tpc) { tpc.cluster() }
}

class Average implements SerializableFunction<TaggedPointCounter, TaggedPointCounter> {
    TaggedPointCounter apply(TaggedPointCounter tpc) { tpc.average() }
}

class Plus implements SerializableBinaryOperator<TaggedPointCounter> {
    TaggedPointCounter apply(TaggedPointCounter tpc1, TaggedPointCounter tpc2) { tpc1 + tpc2 }
}

int k = 5
int iterations = 20

// read in data from our file
var url = WhiskeyWayang.classLoader.getResource('whiskey.csv').file
var pointsData = new File(url).readLines()[1..-1].collect{ Point.fromLine(it) }
var dims = pointsData[0].pts.size()

// create some random points as initial centroids
var r = new Random()
var randomPoint = { (0..<dims).collect { r.nextGaussian() + 2 } as double[] }
var initPts = (1..k).collect(randomPoint)

// create planbuilder with Java and Spark enabled
var configuration = new Configuration()
var context = new WayangContext(configuration)
    .withPlugin(Java.basicPlugin())
    .withPlugin(Spark.basicPlugin())
var planBuilder = new JavaPlanBuilder(context, "KMeans ($url, k=$k, iterations=$iterations)")

var points = planBuilder
    .loadCollection(pointsData).withName('Load points')

var initialCentroids = planBuilder
    .loadCollection((0..<k).collect{ idx -> new TaggedPointCounter(initPts[idx], idx, 0) })
    .withName("Load random centroids")

var finalCentroids = initialCentroids
    .repeat(iterations, currentCentroids ->
        points.map(new SelectNearestCentroid())
            .withBroadcast(currentCentroids, "centroids").withName("Find nearest centroid")
            .reduceByKey(new Cluster(), new Plus()).withName("Add up points")
            .map(new Average()).withName("Average points")
            .withOutputClass(TaggedPointCounter)).withName("Loop").collect()

println 'Centroids:'
finalCentroids.each { c ->
    var pts = c.pts.collect{ sprintf '%.2f', it }.join(', ')
    println "Cluster$c.cluster ($c.count points): $pts"
}
/*
Centroids:
Cluster0 (24 points): 2.79, 2.42, 1.46, 0.04, 0.00, 1.88, 1.67, 1.96, 1.92, 2.08, 2.17, 1.71
Cluster1 (6 points): 3.67, 1.50, 3.67, 3.33, 0.67, 0.17, 1.67, 0.50, 1.17, 1.33, 1.17, 0.17
Cluster2 (15 points): 1.80, 1.93, 1.93, 1.13, 0.20, 1.20, 1.33, 0.80, 1.60, 1.80, 1.00, 1.13
Cluster3 (2 points): 2.00, 1.50, 2.50, 0.50, 0.00, 0.00, 2.50, 0.50, 0.00, 1.00, 2.00, 2.00
Cluster4 (39 points): 1.49, 2.51, 1.05, 0.21, 0.08, 1.10, 1.13, 0.54, 1.26, 1.74, 1.97, 2.13
*/
