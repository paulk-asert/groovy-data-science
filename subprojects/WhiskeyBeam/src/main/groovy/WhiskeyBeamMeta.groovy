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
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.transforms.Combine
import org.apache.beam.sdk.transforms.Create
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.DoFn.Element
import org.apache.beam.sdk.transforms.DoFn.OutputReceiver
import org.apache.beam.sdk.transforms.DoFn.ProcessElement
import org.apache.beam.sdk.transforms.PTransform
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.transforms.View
import org.apache.beam.sdk.values.KV
import org.apache.beam.sdk.values.PCollection

import static java.lang.Math.sqrt
import static java.util.logging.Level.INFO
import static java.util.logging.Level.SEVERE
import static java.util.logging.Logger.getLogger
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV
import static org.apache.commons.math4.legacy.stat.StatUtils.sumSq

static buildPipeline(Pipeline p, String filename, int k, int iterations, int dims) {
    var readCsv = new DoFn<String, Point>() {
        @ProcessElement
        void processElement(@Element String path, OutputReceiver<Point> receiver) throws IOException {
            def parser = CSV.builder().setHeader().setSkipHeaderRecord(true).build()
            def records = new File(path).withReader { rdr -> parser.parse(rdr).records*.toList() }
            records.each { receiver.output(new Point(it[2..-1] as double[])) }
        }
    }

    var pointArray2out = new DoFn<Points, String>() {
        @ProcessElement
        void processElement(@Element Points pts, OutputReceiver<String> out) {
            String log = "Centroids:\n${pts.pts()*.toString().join('\n')}"
            out.output(log)
        }
    }

    var assign = { Point pt, Points centroids ->
        var minDistance = Double.POSITIVE_INFINITY
        var nearestCentroidId = -1
        var idxs = pt.pts().indices
        centroids.pts().eachWithIndex { Point next, int cluster ->
            var distance = sqrt(sumSq(idxs.collect { pt.pts()[it] - next.pts()[it] } as double[]))
            if (distance < minDistance) {
                minDistance = distance
                nearestCentroidId = cluster
            }
        }
        KV.of(nearestCentroidId, pt)
    }

    Points initCentroids = new Points((1..k).collect { Point.ofRandom(dims) })

    var points = p
        | Create.of(filename)
        | 'Read points' >> ParDo.of(readCsv)

    var centroids = p
        | Create.of(initCentroids)

    iterations.times {
        var centroidsView = centroids
            | View.asSingleton()

        centroids = points
            | 'Assign clusters' >> ParDo.of(new AssignClusters(centroidsView, assign)).withSideInputs(centroidsView)
            | 'Calculate new centroids' >> Combine.perKey(new MeanDoubleArrayCols())
            | 'As Points' >> Combine.globally(new Squash(k: k, dims: dims))
    }
    centroids
        | 'Display centroids' >> ParDo.of(pointArray2out) | Log.ofElements()
}

PCollection.metaClass.or = { List arg -> delegate.apply(*arg) }
PCollection.metaClass.or = { PTransform arg -> delegate.apply(arg) }
String.metaClass.rightShift = { PTransform arg -> [delegate, arg] }
Pipeline.metaClass.or = { PTransform arg -> delegate.apply(arg) }

getLogger(getClass().name).info 'Creating pipeline ...'
var pipeline = Pipeline.create()
getLogger('').level = SEVERE // quieten root logging

int k = 5
int iterations = 10
int dims = 12

def csv = getClass().classLoader.getResource('whiskey.csv').path
buildPipeline(pipeline, csv, k, iterations, dims)
getLogger(Log.name).level = INFO // logging on for us
pipeline.run().waitUntilFinish()
