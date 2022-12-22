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
import org.apache.beam.sdk.values.PCollection
import smile.data.DataFrame
import smile.data.formula.Formula
import smile.io.Read
import smile.regression.OLS

import static java.lang.Math.sqrt
import static java.util.logging.Level.INFO
import static java.util.logging.Level.SEVERE
import static java.util.logging.Logger.getLogger
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV
import static org.apache.commons.math3.stat.StatUtils.sumSq
import static smile.math.MathEx.dot

static buildPipeline(Pipeline p, String filename) {
    var features = [
            'price', 'bedrooms', 'bathrooms', 'sqft_living', 'sqft_living15', 'lat',
            'sqft_above', 'grade', 'view', 'waterfront', 'floors'
    ]

    var readCsvChunks = new DoFn<String, double[][]>() {
        @ProcessElement
        void processElement(@Element String path, OutputReceiver<double[][]> receiver) throws IOException {
            var chunkSize = 6000
            var table = Read.csv(new File(path).toPath(), CSV.withFirstRecordAsHeader())
            table = table.select(*features)
            var filtered = table.toList().findAll { it.apply('bedrooms') <= 30 }
            table = DataFrame.of(filtered)
            var idxs = 0..<table.nrow()
            for (nextChunkIdxs in idxs.shuffled().collate(chunkSize)) {
                var all = table.toArray().toList()
                receiver.output(all[nextChunkIdxs] as double[][])
            }
        }
    }

    var fitModel = new DoFn<double[][], double[]>() {
        @ProcessElement
        void processElement(@Element double[][] rows, OutputReceiver<double[]> receiver) throws IOException {
            var model = OLS.fit(Formula.lhs('price'), DataFrame.of(rows, *features))
            receiver.output([model.intercept(), *model.coefficients()] as double[])
        }
    }

    var evalModel = { double[][] chunk, double[] modelParams ->
        double intercept = modelParams[0]
        double[] coefficients = modelParams[1..-1]
        var predicted = chunk.collect { row -> intercept + dot(row[1..-1] as double[], coefficients) }
        var residuals = chunk.toList().indexed().collect { idx, row -> predicted[idx] - row[0] }
        var rmse = sqrt(sumSq(residuals as double[]) / chunk.size())
        [rmse, residuals.average(), chunk.size()] as double[]
    }

    var model2out = new DoFn<double[], String>() {
        @ProcessElement
        void processElement(@Element double[] ds, OutputReceiver<String> out) {
            out.output("** intercept: ${ds[0]}, coeffs: ${ds[1..-1].join(', ')}".toString())
        }
    }

    var stats2out = new DoFn<double[], String>() {
        @ProcessElement
        void processElement(@Element double[] ds, OutputReceiver<String> out) {
            out.output("** rmse: ${ds[0]}, mean: ${ds[1]}, count: ${ds[2]}".toString())
        }
    }

    var csvChunks = p
            | Create.of(filename)
            | 'Create chunks' >> ParDo.of(readCsvChunks)

    var model = csvChunks
            | 'Fit chunks' >> ParDo.of(fitModel)
            | Combine.globally(new MeanDoubleArrayCols())

    var modelView = model
            | View.<double[]>asSingleton()

    csvChunks
            | ParDo.of(new EvaluateModel(modelView, evalModel)).withSideInputs(modelView)
            | Combine.globally(new AggregateModelStats())
            | 'Log stats' >> ParDo.of(stats2out) | Log.ofElements()

    model
            | 'Log model' >> ParDo.of(model2out) | Log.ofElements()
}

PCollection.metaClass.or = { List arg -> delegate.apply(*arg) }
PCollection.metaClass.or = { PTransform arg -> delegate.apply(arg) }
String.metaClass.rightShift = { PTransform arg -> [delegate, arg] }
Pipeline.metaClass.or = { PTransform arg -> delegate.apply(arg) }

getLogger(getClass().name).info 'Creating pipeline ...'
var pipeline = Pipeline.create()
getLogger('').level = SEVERE // quieten root logging

buildPipeline(pipeline, getClass().classLoader.getResource('kc_house_data.csv').path)
getLogger(Log.name).level = INFO // logging on for us
pipeline.run().waitUntilFinish()
