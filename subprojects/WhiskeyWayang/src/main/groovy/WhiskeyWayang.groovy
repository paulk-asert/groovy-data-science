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
import groovy.transform.Canonical
import org.apache.wayang.core.api.Configuration
import org.apache.wayang.core.api.WayangContext
import org.apache.wayang.core.function.ExecutionContext
import org.apache.wayang.core.function.FunctionDescriptor
//import org.apache.wayang.core.optimizer.cardinality.CardinalityEstimate
//import org.apache.wayang.core.optimizer.cardinality.CardinalityEstimator
import org.apache.wayang.java.Java
import org.apache.wayang.api.JavaPlanBuilder
import org.apache.wayang.spark.Spark

import static java.lang.Math.sqrt

class Point { double[] pts }

class TaggedPoint extends Point { int cluster }

@Canonical(includeSuperProperties=true)
class TaggedPointCounter extends TaggedPoint {
    long count
    TaggedPointCounter plus(TaggedPointCounter other) {
        new TaggedPointCounter((0..<pts.size()).collect{ pts[it] + other.pts[it] } as double[], cluster, count + other.count)
    }
    TaggedPointCounter average() {
        new TaggedPointCounter(pts.collect{it/count } as double[], cluster, 0)
    }
}

int k = 5
Integer iterations = 20
def configuration = new Configuration()

def url = WhiskeyWayang.classLoader.getResource('whiskey.csv').file
def context = new WayangContext(configuration)
        .withPlugin(Java.basicPlugin())
        .withPlugin(Spark.basicPlugin())
def planBuilder = new JavaPlanBuilder(context)
        .withJobName("KMeans ($url, k=$k, iterations=$iterations)")

def points = new File(url).readLines()[1..-1].collect{ new Point(pts: it.split(",")[2..-1]*.toDouble()) }
def pointsBuilder = planBuilder.loadCollection(points)

def random = new Random()
double[][] initPts = (1..k).collect{ (0..<points[0].pts.size()).collect{ random.nextGaussian() * 4 } }
def initialCentroidsBuilder = planBuilder
        .loadCollection((0..<k).collect{new TaggedPointCounter(initPts[it], it, 0)})
        .withName("Load random centroids")
//println initialCentroidsBuilder.collect()

class SelectNearestCentroid implements FunctionDescriptor.ExtendedSerializableFunction<Point, TaggedPointCounter> {
    Iterable<TaggedPointCounter> centroids

    @Override
    void open(ExecutionContext executionCtx) {
        centroids = executionCtx.getBroadcast("centroids")
    }

    @Override
    TaggedPointCounter apply(Point point) {
        def minDistance = Double.POSITIVE_INFINITY
        def nearestCentroidId = -1
        for (centroid in centroids) {
            def distance = sqrt((0..<point.pts.size()).collect{ point.pts[it] - centroid.pts[it] }.sum{ it ** 2 })
            if (distance < minDistance) {
                minDistance = distance
                nearestCentroidId = centroid.cluster
            }
        }
        new TaggedPointCounter(point.pts, nearestCentroidId, 1)
    }
}

//CardinalityEstimator kest = { ctx, inp -> new CardinalityEstimate(k, k, 1d) }
def finalCentroids = initialCentroidsBuilder.repeat(iterations, { currentCentroids ->
    pointsBuilder.map(new SelectNearestCentroid())
            .withBroadcast(currentCentroids, "centroids").withName("Find nearest centroid")
            .reduceByKey(TaggedPointCounter::getCluster, TaggedPointCounter::plus).withName("Add up points")
            //.withCardinalityEstimator(kest)
            .map(TaggedPointCounter::average).withName("Average points").withOutputClass(TaggedPointCounter)
}).withName("Loop").collect()

println 'Centroids:'
finalCentroids.each {
    println "Cluster$it.cluster: ${it.pts.collect{ d -> sprintf('%.3f', d)}.join(', ')}"
}
