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
import org.apache.wayang.api.JavaPlanBuilder
import org.apache.wayang.core.api.Configuration
import org.apache.wayang.core.api.WayangContext
import org.apache.wayang.core.function.ExecutionContext
import org.apache.wayang.core.function.FunctionDescriptor.ExtendedSerializableFunction
import org.apache.wayang.core.function.FunctionDescriptor.SerializableBinaryOperator
import org.apache.wayang.core.function.FunctionDescriptor.SerializableFunction
import org.apache.wayang.java.Java
import org.apache.wayang.spark.Spark
import java.util.stream.IntStream

record Point(double[] pts) implements Serializable {
    static Point fromLine(String line) { new Point(line.split(',')[2..-1]*.toDouble() as double[]) }
}

record TaggedPointCounter(double[] pts, int cluster, long count) implements Serializable {
    TaggedPointCounter plus(TaggedPointCounter that) {
        new TaggedPointCounter(
                IntStream.range(0, pts.size()).mapToDouble(idx -> pts[idx] + that.pts[idx]).toArray(),
                cluster,
                this.count + that.count)
    }

    TaggedPointCounter average() {
        new TaggedPointCounter(
                Arrays.stream(pts).mapToObj(d -> d / count).mapToDouble(d -> d).toArray(),
                cluster,
                0)
    }
}

@CompileStatic
class SelectNearestCentroid implements ExtendedSerializableFunction<Point, TaggedPointCounter> {
    Iterable<TaggedPointCounter> centroids

    void open(ExecutionContext context) {
        centroids = context.getBroadcast("centroids")
    }

    TaggedPointCounter apply(Point point) {
        def minDistance = Double.POSITIVE_INFINITY
        def nearestCentroidId = -1
        for (centroid in centroids) {
            def distance = Math.sqrt(IntStream.range(0, point.pts.size())
                    .mapToDouble(idx -> point.pts[idx] - centroid.pts[idx])
                    .map(d -> d * d).sum())
            if (distance < minDistance) {
                minDistance = distance
                nearestCentroidId = centroid.cluster
            }
        }
        new TaggedPointCounter(point.pts, nearestCentroidId, 1)
    }
}

@CompileStatic
class Cluster implements SerializableFunction<TaggedPointCounter, Integer> {
    Integer apply(TaggedPointCounter tpc) { tpc.cluster() }
}

@CompileStatic
class Average implements SerializableFunction<TaggedPointCounter, TaggedPointCounter> {
    TaggedPointCounter apply(TaggedPointCounter tpc) { tpc.average() }
}

@CompileStatic
class Plus implements SerializableBinaryOperator<TaggedPointCounter> {
    TaggedPointCounter apply(TaggedPointCounter tpc1, TaggedPointCounter tpc2) { tpc1.plus(tpc2) }
}

int k = 5
int iterations = 20
def configuration = new Configuration()

def url = WhiskeyWayang.classLoader.getResource('whiskey.csv').file
def context = new WayangContext(configuration)
        .withPlugin(Java.basicPlugin())
        .withPlugin(Spark.basicPlugin())
def planBuilder = new JavaPlanBuilder(context, "KMeans ($url, k=$k, iterations=$iterations)")

def pointsData = new File(url).readLines()[1..-1].collect(line -> Point.fromLine(line))
def dims = pointsData[0].pts().size()
def points = planBuilder.loadCollection(pointsData).withName('Load points')

def r = new Random()
def initPts = (1..k).collect(cluster -> (0..<dims).collect(row -> r.nextGaussian() + 2) as double[])
def initialCentroids = planBuilder
        .loadCollection((0..<k).collect(idx -> new TaggedPointCounter(initPts[idx], idx, 0)))
        .withName("Load random centroids")

def next = currentCentroids ->
        points.map(new SelectNearestCentroid())
                .withBroadcast(currentCentroids, "centroids").withName("Find nearest centroid")
                .reduceByKey(new Cluster(), new Plus()).withName("Add up points")
                .map(new Average()).withName("Average points")
                .withOutputClass(TaggedPointCounter)

def finalCentroids = initialCentroids
        .repeat(iterations, next).withName("Loop").collect()

println 'Centroids:'
finalCentroids.each {
    println "Cluster${it.cluster()}: ${it.pts().collect { d -> sprintf('%.3f', d) }.join(', ')}"
}
