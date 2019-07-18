import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.transforms.Combine
import org.apache.beam.sdk.transforms.Create
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.DoFn.Element
import org.apache.beam.sdk.transforms.DoFn.OutputReceiver
import org.apache.beam.sdk.transforms.DoFn.ProcessElement
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.transforms.View
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.PCollectionView
import org.apache.commons.math3.stat.StatUtils
import smile.math.Math
import smile.regression.OLS
import tech.tablesaw.api.Table
import util.Log

import static java.lang.Math.sqrt


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

class MeanDoubleArrayCols implements SerializableFunction<Iterable<double[]>, double[]> {
    @Override
    double[] apply(Iterable<double[]> input) {
        double[] sum = null
        def count = 0
        for (double[] next : input) {
            if (sum == null) {
                sum = new double[next.size()]
                (0..<sum.size()).each{ sum[it] = 0.0d }
            }
            (0..<sum.size()).each{ sum[it] += next[it] }
            count++
        }
        if (sum != null) (0..<sum.size()).each{ sum[it] /= count }
        return sum
    }
}

class AggregateModelStats implements SerializableFunction<Iterable<double[]>, double[]> {
    @Override
    double[] apply(Iterable<double[]> input) {
        double[] sum = null
        for (double[] next : input) {
            if (sum == null) {
                sum = new double[next.size()]
                (0..<sum.size()).each{ sum[it] = 0.0d }
            }
            def total = sum[2] + next[2]
            sum[0] = Math.sqrt((sum[2] * sum[0] * sum[0] + next[2] * next[0] * next[0]) / total)
            sum[1] = (sum[2] * sum[1] + next[2] * next[1]) / total
            sum[2] = total
        }
        return sum
    }
}

static buildPipeline(Pipeline p) {
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
            Collections.shuffle(idxs.toList())
            for (nextChunkIdxs in idxs.collate(chunkSize)) {
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
        def predicted = chunk.collect { row -> intercept + Math.dot(row[1..-1] as double[], coefficients) }
        def residuals = chunk.toList().indexed().collect { idx, row -> predicted[idx] - row[0] }
        def rmse = sqrt(StatUtils.sumSq(residuals as double[]) / chunk.size())
        def mean = Math.mean(residuals as double[])
        [rmse, mean, chunk.size()] as double[]
    }

    def model2out = new DoFn<double[], String>() {
        @ProcessElement
        void processElement(@Element double[] ds, OutputReceiver<String> out) {
            sleep 10000
            out.output("** intercept: ${ds[0]}, coeffs: ${ds[1..-1].join(', ')}".toString())
        }
    }

    def stats2out = new DoFn<double[], String>() {
        @ProcessElement
        void processElement(@Element double[] ds, OutputReceiver<String> out) {
            sleep 5000
            out.output("** rmse: ${ds[0]}, mean: ${ds[1]}, count: ${ds[2]}".toString())
        }
    }

    def csvChunks = p
            .apply(Create.of('/path/to/kc_house_data.csv'))
            .apply('Create chunks', ParDo.of(readCsvChunks))
    def model = csvChunks
            .apply('Fit chunks', ParDo.of(fitModel))
            .apply(Combine.globally(new MeanDoubleArrayCols()))
    def modelView = model
            .apply(View.<double[]>asSingleton())
    csvChunks
            .apply(ParDo.of(new EvaluateModel(modelView, evalModel)).withSideInputs(modelView))
            .apply(Combine.globally(new AggregateModelStats()))
            .apply(ParDo.of(stats2out)).apply(Log.ofElements())
    model
            .apply(ParDo.of(model2out)).apply(Log.ofElements())
}

def pipeline = Pipeline.create()
buildPipeline(pipeline)
pipeline.run().waitUntilFinish()
