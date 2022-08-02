import ai.djl.Application
import ai.djl.ndarray.NDArrays
import ai.djl.ndarray.NDList
import ai.djl.repository.zoo.Criteria
import ai.djl.training.util.ProgressBar
import ai.djl.translate.NoBatchifyTranslator
import ai.djl.translate.TranslatorContext
import smile.plot.swing.Heatmap
import smile.plot.swing.Palette

import static smile.math.MathEx.dot

/*
 * An example of inference using an universal sentence encoder model from TensorFlow Hub.
 * For more info see: https://tfhub.dev/google/universal-sentence-encoder/4
 * Inspired by: https://github.com/deepjavalibrary/djl/blob/master/examples/src/main/java/ai/djl/examples/inference/UniversalSentenceEncoder.java
 */

class MyTranslator implements NoBatchifyTranslator<String[], double[][]> {
    @Override
    NDList processInput(TranslatorContext ctx, String[] raw) {
        var factory = ctx.NDManager
        var inputs = new NDList(raw.collect(factory::create))
        new NDList(NDArrays.stack(inputs))
    }

    @Override
    double[][] processOutput(TranslatorContext ctx, NDList list) {
        long numOutputs = list.singletonOrThrow().shape.get(0)
        NDList result = []
        for (i in 0..<numOutputs) {
            result << list.singletonOrThrow().get(i)
        }
        result*.toFloatArray() as double[][]
    }
}

def predict(String[] inputs) {
    String modelUrl = "https://storage.googleapis.com/tfhub-modules/google/universal-sentence-encoder/4.tar.gz"

    Criteria<String[], double[][]> criteria =
        Criteria.builder()
            .optApplication(Application.NLP.TEXT_EMBEDDING)
            .setTypes(String[], double[][])
            .optModelUrls(modelUrl)
            .optTranslator(new MyTranslator())
            .optEngine("TensorFlow")
            .optProgress(new ProgressBar())
            .build()
    try (var model = criteria.loadModel()
         var predictor = model.newPredictor()) {
        predictor.predict(inputs)
    }
}
String[] inputs = [
    "Cycling is low impact and great for cardio",
    "Swimming is low impact and good for fitness",
    "Palates is good for fitness and flexibility",
    "Weights are good for strength and fitness",
    "Orchids can be tricky to grow",
    "Sunflowers are fun to grow",
    "Radishes are easy to grow",
    "The taste of radishes grows on you after a while",
]
var k = inputs.size()

var embeddings = predict(inputs)

def z = new double[k][k]
for (i in 0..<k) {
    println "Embedding for: ${inputs[i]}\n${Arrays.toString(embeddings[i])}"
    for (j in 0..<k) {
        z[i][j] = dot(embeddings[i], embeddings[j])
    }
}

new Heatmap(inputs, inputs, z, Palette.heat(20).reverse()).canvas().with {
    title = 'Semantic textual similarity'
    setAxisLabels('', '')
    window()
}
