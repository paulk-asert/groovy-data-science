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
import util.Log

import static java.lang.Math.sqrt
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV
import static org.apache.commons.math3.stat.StatUtils.sumSq
import static smile.math.MathEx.dot

//interface Options extends PipelineOptions {
//    @Description("Input file path")
//    @Default.String('/path/to/kc_house_data.csv')
//    String getInputFile()
//    void setInputFile(String value)
//
//    @Description("Output directory")
//    @Required
//    String getOutputDir()
//    void setOutputDir(String value)
//}

static buildPipeline(Pipeline p, String filename) {
    def features = [
            'price', 'bedrooms', 'bathrooms', 'sqft_living', 'sqft_living15', 'lat',
            'sqft_above', 'grade', 'view', 'waterfront', 'floors'
    ]

    def readCsvChunks = new DoFn<String, double[][]>() {
        @ProcessElement
        void processElement(@Element String path, OutputReceiver<double[][]> receiver) throws IOException {
            def chunkSize = 6000
            def table = Read.csv(new File(path).toPath(), CSV.withFirstRecordAsHeader())
            table = table.select(*features)
            table = table.stream().filter { it.apply('bedrooms') <= 30 }.collect(DataFrame.collect())
            def idxs = 0..<table.nrows()
            for (nextChunkIdxs in idxs.shuffled().collate(chunkSize)) {
                def all = table.toArray().toList()
                receiver.output(all[nextChunkIdxs] as double[][])
            }
            sleep 2000
        }
    }

    def fitModel = new DoFn<double[][], double[]>() {
        @ProcessElement
        void processElement(@Element double[][] rows, OutputReceiver<double[]> receiver) throws IOException {
            def model = OLS.fit(Formula.lhs('price'), DataFrame.of(rows, features as String[])).coefficients()
            receiver.output(model)
        }
    }

    def evalModel = { double[][] chunk, double[] model ->
        double intercept = model[0]
        double[] coefficients = model[1..-1]
        def predicted = chunk.collect { row -> intercept + dot(row[1..-1] as double[], coefficients) }
        def residuals = chunk.toList().indexed().collect { idx, row -> predicted[idx] - row[0] }
        def rmse = sqrt(sumSq(residuals as double[]) / chunk.size())
        [rmse, residuals.average(), chunk.size()] as double[]
    }

    def model2out = new DoFn<double[], String>() {
        @ProcessElement
        void processElement(@Element double[] ds, OutputReceiver<String> out) {
            sleep 6000 // push to end of log so easier to find
            out.output("** intercept: ${ds[0]}, coeffs: ${ds[1..-1].join(', ')}".toString())
        }
    }

    def stats2out = new DoFn<double[], String>() {
        @ProcessElement
        void processElement(@Element double[] ds, OutputReceiver<String> out) {
            sleep 4000 // push to end of log so easier to find
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

def pipeline = Pipeline.create()
//buildPipeline(pipeline, '/path/to/kc_house_data.csv')
buildPipeline(pipeline, getClass().classLoader.getResource('kc_house_data.csv').path)
pipeline.run().waitUntilFinish()
