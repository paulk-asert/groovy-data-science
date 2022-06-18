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
import groovy.transform.TupleConstructor
import org.apache.wayang.api.DataQuantaBuilder
import org.apache.wayang.api.LoadCollectionDataQuantaBuilder
import org.apache.wayang.core.api.Configuration
import org.apache.wayang.core.api.WayangContext
import org.apache.wayang.core.function.ExecutionContext
import org.apache.wayang.core.function.FunctionDescriptor.ExtendedSerializableFunction
//import org.apache.wayang.core.optimizer.cardinality.CardinalityEstimate
//import org.apache.wayang.core.optimizer.cardinality.CardinalityEstimator

import org.apache.wayang.api.JavaPlanBuilder
//import org.apache.wayang.java.Java
import org.apache.wayang.java.Java
import org.apache.wayang.spark.Spark

import java.util.function.Function
import java.util.stream.IntStream

import static java.lang.Math.sqrt

record Point(double[] pts) implements Serializable { }

record TaggedPointCounter(double[] pts, int cluster, long count) implements Serializable {
    TaggedPointCounter plus(TaggedPointCounter that) {
        new TaggedPointCounter(Util.sumPairs(this, that), cluster, this.count + that.count)
    }


    TaggedPointCounter average() {
        new TaggedPointCounter(Util.averagedPoint(this), cluster, 0)
    }
}

@CompileStatic
class Util implements Serializable {
    static double[] averagedPoint(TaggedPointCounter tpc) {
        Arrays.stream(tpc.pts).mapToObj(d -> d / tpc.count).mapToDouble(d -> d).toArray()
    }

    static double[] sumPairs(TaggedPointCounter tpc1, TaggedPointCounter tpc2) {
        IntStream.range(0, tpc1.pts.size()).mapToDouble(idx -> tpc1.pts[idx] + tpc2.pts[idx]).toArray()
    }

    static double calcDistance(Point pt, TaggedPointCounter centroid) {
        sqrt(IntStream.range(0, pt.pts.size())
                .mapToDouble(idx -> pt.pts[idx] - centroid.pts[idx])
                .map(d -> d * d).sum())
    }
}

int k = 5
Integer iterations = 20
def configuration = new Configuration()

def url = WhiskeyWayang.classLoader.getResource('whiskey.csv').file
def context = new WayangContext(configuration)
        .withPlugin(Java.basicPlugin())
        .withPlugin(Spark.basicPlugin())
def planBuilder = new JavaPlanBuilder(context, "KMeans ($url, k=$k, iterations=$iterations)")

def points = new File(url).readLines()[1..-1].collect{ new Point(it.split(",")[2..-1]*.toDouble() as double[]) }
def dims = points[0].pts.size()
def pointsBuilder = planBuilder.loadCollection(points)

def random = new Random()
double[][] initPts = (1..k).collect{ (0..<dims).collect{ random.nextGaussian() * 4 } }
def initialCentroidsBuilder = planBuilder
        .loadCollection((0..<k).collect{new TaggedPointCounter(initPts[it], it, 0)})
        .withName("Load random centroids")
//println initialCentroidsBuilder.collect()

class SelectNearestCentroid implements ExtendedSerializableFunction<Point, TaggedPointCounter> {
    Iterable<TaggedPointCounter> centroids

    @Override
    void open(ExecutionContext context) {
        centroids = context.getBroadcast("centroids")
    }

    @Override
    TaggedPointCounter apply(Point point) {
        def minDistance = Double.POSITIVE_INFINITY
        def nearestCentroidId = -1
        for (centroid in centroids) {
            def distance = Util.calcDistance(point, centroid)
            if (distance < minDistance) {
                minDistance = distance
                nearestCentroidId = centroid.cluster
            }
        }
        new TaggedPointCounter(point.pts, nearestCentroidId, 1)
    }
}

//org.codehaus.groovy.runtime.MethodClosure.ALLOW_RESOLVE = true

@CompileStatic @TupleConstructor
class KMeansStep implements Function<DataQuantaBuilder, DataQuantaBuilder>, Serializable {
    LoadCollectionDataQuantaBuilder builder

    DataQuantaBuilder apply(DataQuantaBuilder currentCentroids) {
        builder.map(new SelectNearestCentroid())
                .withBroadcast(currentCentroids, "centroids").withName("Find nearest centroid")
                .reduceByKey(TaggedPointCounter::cluster, TaggedPointCounter::plus).withName("Add up points")
                .map(TaggedPointCounter::average).withName("Average points").withOutputClass(TaggedPointCounter)
    }
}

def finalCentroids = initialCentroidsBuilder.repeat(iterations, new KMeansStep(pointsBuilder)).withName("Loop").collect()

println 'Centroids:'
finalCentroids.each {
    println "Cluster$it.cluster: ${it.pts.collect{ d -> sprintf('%.3f', d)}.join(', ')}"
}
