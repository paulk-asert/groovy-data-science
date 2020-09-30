import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.transforms.Combine
import org.apache.beam.sdk.transforms.Create
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.DoFn.Element
import org.apache.beam.sdk.transforms.DoFn.OutputReceiver
import org.apache.beam.sdk.transforms.DoFn.ProcessElement
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.transforms.View
import org.apache.commons.math3.stat.StatUtils
import smile.regression.OLS
import tech.tablesaw.api.Table
import util.Log

import static java.lang.Math.sqrt
import static smile.math.Math.dot

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
            def table = Table.read().csv(path)
            table = table.dropWhere(table.column("bedrooms").isGreaterThan(30))
            def idxs = 0..<table.rowCount()
            for (nextChunkIdxs in idxs.shuffled().collate(chunkSize)) {
                def chunk = table.rows(*nextChunkIdxs)
                receiver.output(chunk.as().doubleMatrix(*features))
            }
            sleep 2000
        }
    }

    def fitModel = new DoFn<double[][], double[]>() {
        @ProcessElement
        void processElement(@Element double[][] rows, OutputReceiver<double[]> receiver) throws IOException {
            double[] model = new OLS(rows.collect{ it[1..-1] } as double[][], rows.collect{ it[0] } as double[]).with{ [it.intercept(), *it.coefficients()] }
            receiver.output(model)
        }
    }

    def evalModel = { double[][] chunk, double[] model ->
        double intercept = model[0]
        double[] coefficients = model[1..-1]
        def predicted = chunk.collect { row -> intercept + dot(row[1..-1] as double[], coefficients) }
        def residuals = chunk.toList().indexed().collect { idx, row -> predicted[idx] - row[0] }
        def rmse = sqrt(StatUtils.sumSq(residuals as double[]) / chunk.size())
        def mean = residuals.average()
        [rmse, mean, chunk.size()] as double[]
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
            .apply(Create.of(filename))
            .apply('Create chunks', ParDo.of(readCsvChunks))
    var model = csvChunks
            .apply('Fit chunks', ParDo.of(fitModel))
            .apply(Combine.globally(new MeanDoubleArrayCols()))
    var modelView = model
            .apply(View.<double[]>asSingleton())
    csvChunks
            .apply(ParDo.of(new EvaluateModel(modelView, evalModel)).withSideInputs(modelView))
            .apply(Combine.globally(new AggregateModelStats()))
            .apply('Log stats', ParDo.of(stats2out)).apply(Log.ofElements())
    model
            .apply('Log model', ParDo.of(model2out)).apply(Log.ofElements())
}

def pipeline = Pipeline.create()
//buildPipeline(pipeline, '/path/to/kc_house_data.csv')
buildPipeline(pipeline, getClass().classLoader.getResource('kc_house_data.csv').path)
pipeline.run().waitUntilFinish()
