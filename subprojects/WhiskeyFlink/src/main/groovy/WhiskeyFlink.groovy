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
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.connector.file.src.FileSource
import org.apache.flink.connector.file.src.reader.TextLineInputFormat
import org.apache.flink.core.fs.Path
import org.apache.flink.ml.clustering.kmeans.KMeans
import org.apache.flink.ml.clustering.kmeans.KMeansModelData
import org.apache.flink.ml.linalg.DenseVector
import org.apache.flink.ml.linalg.Vectors
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment
import org.apache.flink.types.Row
import org.apache.flink.util.CloseableIterator
import org.apache.flink.util.Collector

@CompileStatic
class WhiskeyFlink {
    static FlatMapFunction<String, DenseVector> splitAndChop = (String s, Collector out) ->
        out.collect(Vectors.dense(s.split(',')[2..-1] as double[]))
    static headerSkipped = false

    static void main(args) {
        var file = WhiskeyFlink.classLoader.getResource('whiskey.csv').file
        var eEnv = StreamExecutionEnvironment.executionEnvironment
        var tEnv = StreamTableEnvironment.create(eEnv)

        var source = FileSource.forRecordStreamFormat(new TextLineInputFormat(), new Path(file)).build()
        var stream = eEnv
            .fromSource(source, WatermarkStrategy.noWatermarks(), "csvfile")
            .filter(s -> { var saved = headerSkipped; headerSkipped = true; saved })
            .flatMap(splitAndChop)

        var inputTable = tEnv.fromDataStream(stream).limit(1, 86).as("features")

        var kmeans = new KMeans(k: 3, seed: 1L)

        var kmeansModel = kmeans.fit(inputTable)

        var outputTable = kmeansModel.transform(inputTable)[0]

        var clusters = [:].withDefault{ [] }
        outputTable.execute().collect().each { row ->
            var features = row.getField(kmeans.featuresCol)
            var clusterId = row.getField(kmeans.predictionCol)
            clusters[clusterId] << features
        }
        clusters.each { k, v ->
            println "Cluster ID: $k"
            println "Features list:\n${v.join('\n')}"
        }
    }
}
