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
import org.apache.flink.api.common.functions.FilterFunction
import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.api.java.typeutils.RowTypeInfo
import org.apache.flink.connector.file.src.FileSource
import org.apache.flink.connector.file.src.reader.TextLineInputFormat
import org.apache.flink.core.fs.Path
import org.apache.flink.ml.clustering.kmeans.KMeansModelData
import org.apache.flink.ml.clustering.kmeans.OnlineKMeans
import org.apache.flink.ml.linalg.DenseVector
import org.apache.flink.ml.linalg.Vectors
import org.apache.flink.ml.linalg.typeinfo.DenseVectorTypeInfo
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.table.api.Table
import org.apache.flink.types.Row
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment
import org.apache.flink.util.Collector

@CompileStatic
class WhiskeyFlinkOnlineKmeans {
    static FlatMapFunction<String, DenseVector> splitAndChop = (String s, Collector out) ->
        out.collect(Vectors.dense(s.split(',')[2..-1] as double[]))
    static firstRow = true
    static FilterFunction skipHeader = _ -> { var keep = !firstRow; firstRow = false; keep }

    static void main(args) {
        var file = WhiskeyFlinkOnlineKmeans.classLoader.getResource('whiskey.csv').file
        var eEnv = StreamExecutionEnvironment.executionEnvironment
        eEnv.parallelism = 4
        var tEnv = StreamTableEnvironment.create(eEnv)
        def randomInit = KMeansModelData.generateRandomModelData(tEnv, 3, 12, 0.0d, 0L)

        var source = FileSource.forRecordStreamFormat(new TextLineInputFormat(), new Path(file)).build()
        var stream = eEnv
            .fromSource(source, WatermarkStrategy.noWatermarks(), "csvfile")
            .filter(skipHeader)
            .flatMap(splitAndChop)
        var data = stream.executeAndCollect().collect{ Row.of(it) }
        var train = data[0..79]
        var predict = data[80..-1]

        var trainSource = new PeriodicSourceFunction(1000, train.collate(8))
        var trainStream = eEnv.addSource(trainSource, new RowTypeInfo(DenseVectorTypeInfo.INSTANCE))
        var trainTable = tEnv.fromDataStream(trainStream).as("features")

        var predictSource = new PeriodicSourceFunction(1000, [predict])
        var predictStream = eEnv.addSource(predictSource, new RowTypeInfo(DenseVectorTypeInfo.INSTANCE))
        var predictTable = tEnv.fromDataStream(predictStream).as("features")

        var kmeans = new OnlineKMeans(featuresCol: 'features', predictionCol: 'prediction',
            globalBatchSize: 8, initialModelData: randomInit, k: 3)
        var kmeansModel = kmeans.fit(trainTable)
        var outputTable = kmeansModel.transform(predictTable)[0]
        outputTable.execute().collect().each { row ->
            DenseVector features = (DenseVector) row.getField(kmeans.featuresCol)
            var clusterId = row.getField(kmeans.predictionCol)
            println "Cluster $clusterId: ${features}"
        }
    }
}

class PeriodicSourceFunction implements SourceFunction<Row> {
    private final long interval;

    private final List<List<Row>> data;

    private int index = 0;

    private boolean isRunning = true;

    /**
     * @param interval The time interval in milliseconds to collect data into sourceContext.
     * @param data The data to be collected. Each element is a list of records to be collected
     *     between two adjacent intervals.
     */
    PeriodicSourceFunction(long interval, List<List<Row>> data) {
        this.interval = interval;
        this.data = data;
    }

    @Override
    public void run(SourceFunction.SourceContext<Row> sourceContext) throws Exception {
        while (isRunning) {
            for (Row data : this.data.get(index)) {
                sourceContext.collect(data);
            }
            Thread.sleep(interval)
            println()
            index = (index + 1) % this.data.size();
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}
