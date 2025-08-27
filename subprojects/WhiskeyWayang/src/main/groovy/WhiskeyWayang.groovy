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
// https://github.com/apache/incubator-wayang/blob/011859f8bd2b8f05a4ffc2d71c73edf1dd893fac/README.md#k-means
// See also a way to resurrect "lost" (no points selected) centroids:
// https://github.com/apache/incubator-wayang/blob/9071f6c3d47611657407a9287923294cb2b07e8c/wayang-benchmark/src/main/scala/org/apache/wayang/apps/kmeans/Kmeans.scala#L79

import org.apache.wayang.api.JavaPlanBuilder
import org.apache.wayang.core.api.WayangContext
import org.apache.wayang.core.function.ExecutionContext
import org.apache.wayang.core.function.FunctionDescriptor.ExtendedSerializableFunction
import org.apache.wayang.core.function.FunctionDescriptor.SerializableBinaryOperator
import org.apache.wayang.core.function.FunctionDescriptor.SerializableFunction
import org.apache.wayang.java.Java
import org.apache.wayang.spark.Spark
import static java.lang.Math.sqrt

record Point(double[] pts) implements Serializable { }

record PointGrouping(double[] pts, int cluster, long count) implements Serializable {
    PointGrouping(List<Double> pts, int cluster, long count) {
        this(pts as double[], cluster, count)
    }

    PointGrouping plus(PointGrouping that) {
        var newPts = pts.indices.collect{ pts[it] + that.pts[it] }
        new PointGrouping(newPts, cluster, count + that.count)
    }

    PointGrouping average() {
        new PointGrouping(pts.collect{ double d -> d/count }, cluster, 1)
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
            var distance = sqrt(p.pts.indices
                .collect{ p.pts[it] - c.pts[it] }
                .sum{ it ** 2 } as double)
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
def rows = new File(url).readLines()[1..-1]*.split(',')
var distilleries = rows*.getAt(1)
var pointsData = rows.collect{ new Point(it[2..-1] as double[]) }
var dims = pointsData[0].pts.size()

// create some random points as initial centroids
var r = new Random()
var randomPoint = { (0..<dims).collect { r.nextGaussian() + 2 } as double[] }
var initPts = (1..k).collect(randomPoint)

var context = new WayangContext()
    .withPlugin(Java.basicPlugin()) // comment out this line to force Spark
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
).withName('Loop').collect()

println 'Centroids:'
finalCentroids.each { c ->
    println "Cluster $c.cluster: ${c.pts.collect('%.2f'::formatted).join(', ')}"
}

println()
var allocator = new SelectNearestCentroid(centroids: finalCentroids)
var allocations = pointsData.withIndex()
    .collect{ pt, idx -> [allocator.apply(pt).cluster, distilleries[idx]] }
    .groupBy{ cluster, ds -> "Cluster $cluster" }
    .collectValues{ v -> v.collect{ it[1] } }
    .sort{ it.key }
allocations.each{ c, ds -> println "$c (${ds.size()} members): ${ds.join(', ')}" }
/*
Centroids:
Cluster 0: 2.53, 1.65, 2.76, 2.12, 0.29, 0.65, 1.65, 0.59, 1.35, 1.41, 1.35, 0.94
Cluster 2: 3.33, 2.56, 1.67, 0.11, 0.00, 1.89, 1.89, 2.78, 2.00, 1.89, 2.33, 1.33
Cluster 3: 1.42, 2.47, 1.03, 0.22, 0.06, 1.00, 1.03, 0.47, 1.19, 1.72, 1.92, 2.08
Cluster 4: 2.25, 2.38, 1.38, 0.08, 0.13, 1.79, 1.54, 1.33, 1.75, 2.17, 1.75, 1.79

Cluster 0 (17 members): Ardbeg, Balblair, Bowmore, Bruichladdich, Caol Ila, Clynelish, GlenGarioch, GlenScotia, Highland Park, Isle of Jura, Lagavulin, Laphroig, Oban, OldPulteney, Springbank, Talisker, Teaninich
Cluster 2 (9 members): Aberlour, Balmenach, Dailuaine, Dalmore, Glendronach, Glenfarclas, Macallan, Mortlach, RoyalLochnagar
Cluster 3 (36 members): AnCnoc, ArranIsleOf, Auchentoshan, Aultmore, Benriach, Bladnoch, Bunnahabhain, Cardhu, Craigganmore, Dalwhinnie, Dufftown, GlenElgin, GlenGrant, GlenMoray, GlenSpey, Glenallachie, Glenfiddich, Glengoyne, Glenkinchie, Glenlossie, Glenmorangie, Inchgower, Linkwood, Loch Lomond, Mannochmore, Miltonduff, RoyalBrackla, Speyburn, Speyside, Strathmill, Tamdhu, Tamnavulin, Tobermory, Tomintoul, Tomore, Tullibardine
Cluster 4 (24 members): Aberfeldy, Ardmore, Auchroisk, Belvenie, BenNevis, Benrinnes, Benromach, BlairAthol, Craigallechie, Deanston, Edradour, GlenDeveronMacduff, GlenKeith, GlenOrd, Glendullan, Glenlivet, Glenrothes, Glenturret, Knochando, Longmorn, OldFettercairn, Scapa, Strathisla, Tomatin
*/
