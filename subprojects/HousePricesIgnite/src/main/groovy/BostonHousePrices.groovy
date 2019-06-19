// download ignite and point project lib
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.ml.dataset.feature.extractor.Vectorizer
import org.apache.ignite.ml.dataset.feature.extractor.impl.DummyVectorizer
import org.apache.ignite.ml.regressions.linear.LinearRegressionLSQRTrainer
import org.apache.ignite.ml.regressions.linear.LinearRegressionModel
import org.apache.ignite.ml.selection.scoring.evaluator.Evaluator
import org.apache.ignite.ml.selection.scoring.metric.regression.RegressionMetrics
import org.apache.ignite.ml.selection.split.TrainTestDatasetSplitter
import org.apache.ignite.ml.util.SandboxMLCache

import static org.apache.ignite.ml.environment.LearningEnvironmentBuilder.defaultBuilder
import static org.apache.ignite.ml.util.MLSandboxDatasets.BOSTON_HOUSE_PRICES

/*
 * Example of using Linear Regression model in Apache Ignite for house prices prediction.
 *
 * Description of model can be found in: https://en.wikipedia.org/wiki/Linear_regression .
 * Original dataset can be downloaded from: https://archive.ics.uci.edu/ml/machine-learning-databases/housing/ .
 * Copy of dataset are stored in: modules/ml/src/main/resources/datasets/boston_housing_dataset.txt .
 * Score for regression estimation: R^2 (coefficient of determination).
 * Description of score evaluation can be found in: https://stattrek.com/statistics/dictionary.aspx?definition=coefficient_of_determination .
 */
Ignite ignite = Ignition.start("C:/Temp/example-ignite.xml")
ignite.withCloseable {
    println ">>> Ignite grid started."
    def dataCache = new SandboxMLCache(ignite).fillCacheWith(BOSTON_HOUSE_PRICES)
    def trainer = new LinearRegressionLSQRTrainer()
            .withEnvironmentBuilder(defaultBuilder().withRNGSeed(0))
    def vectorizer = new DummyVectorizer().labeled(Vectorizer.LabelCoordinate.FIRST)
    def split = new TrainTestDatasetSplitter().split(0.8)
    def mdl = trainer.fit(ignite, dataCache, split.trainFilter, vectorizer)
    def metric = new RegressionMetrics().withMetric { it.r2() }
    def score = Evaluator.evaluate(dataCache, split.testFilter, mdl, vectorizer, metric)

    println ">>> Model: " + toString(mdl)
    println ">>> R^2 score: " + score
    dataCache.destroy()
}

static toString(mdl) {
    def sign = { val -> val < 0 ? '- ' : '+ ' }
    def valIdx = { idx, val -> sprintf '%.2f*f%d', val, idx }
    def valIdxSign = { idx, val -> sign(val) + valIdx(idx, Math.abs(val)) }
    def valSign = { val -> sign(val) + sprintf('%.2f', Math.abs(val)) }
    def w = mdl.weights
    def i = mdl.intercept
    def result = [valIdx(0, w.get(0)), *(1..<w.size()).collect{ valIdxSign(it, w.get(it)) }, valSign(i)]
    result.join(' ')
}
