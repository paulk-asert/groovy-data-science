import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.ml.dataset.feature.extractor.Vectorizer
import org.apache.ignite.ml.dataset.feature.extractor.impl.DoubleArrayVectorizer
import org.apache.ignite.ml.regressions.linear.LinearRegressionLSQRTrainer as Trainer
import org.apache.ignite.ml.selection.scoring.evaluator.Evaluator
import org.apache.ignite.ml.selection.scoring.metric.regression.RegressionMetrics as RM
import org.apache.ignite.ml.selection.split.TrainTestDatasetSplitter
import org.apache.ignite.ml.util.SandboxMLCache
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder
import tech.tablesaw.api.Table

import static org.apache.ignite.ml.environment.LearningEnvironmentBuilder.defaultBuilder

def file = getClass().classLoader.getResource('kc_house_data.csv').file
def rows = Table.read().csv(file)
//def rows = Table.read().csv('kc_house_data.csv')
rows = rows.dropWhere(rows.column("bedrooms").isGreaterThan(30))
String[] features = [
        'price', 'bedrooms', 'bathrooms', 'sqft_living', 'sqft_living15',
        'lat', 'sqft_above', 'grade', 'view', 'waterfront', 'floors'
]
def data = rows.as().doubleMatrix(features)

// configure to all run on local machine but could be a cluster (can be hidden in XML)
def cfg = new IgniteConfiguration(
        peerClassLoadingEnabled: true,
        discoverySpi: new TcpDiscoverySpi(
                ipFinder: new TcpDiscoveryMulticastIpFinder(
                        addresses: ['127.0.0.1:47500..47509']
                )
        )
)

static pretty(mdl, features) {
    def sign = { val -> val < 0 ? '- ' : '+ ' }
    def valIdx = { idx, val -> sprintf '%.2f*%s', val, features[idx+1] }
    def valIdxSign = { idx, val -> sign(val) + valIdx(idx, Math.abs(val)) }
    def valSign = { val -> sign(val) + sprintf('%.2f', Math.abs(val)) }
    def (w, i) = [mdl.weights, mdl.intercept]
    def result = [valIdx(0, w.get(0)), *(1..<w.size()).collect{ valIdxSign(it, w.get(it)) }, valSign(i)]
    result.join(' ')
}

Ignition.start(cfg).withCloseable { ignite ->
    println ">>> Ignite grid started for data: ${data.size()} rows X ${data[0].size()} cols"

    def dataCache = new SandboxMLCache(ignite).fillCacheWith(data)
    def trainer = new Trainer().withEnvironmentBuilder(defaultBuilder().withRNGSeed(0))
    def vectorizer = new DoubleArrayVectorizer().labeled(Vectorizer.LabelCoordinate.FIRST)
    def split = new TrainTestDatasetSplitter().split(0.8)
    def mdl = trainer.fit(ignite, dataCache, split.trainFilter, vectorizer)
    def rr = Evaluator.evaluate(dataCache, split.testFilter, mdl, vectorizer, new RM().withMetric { it.r2() })
    def rmse = Evaluator.evaluate(dataCache, split.testFilter, mdl, vectorizer, new RM())
    dataCache.destroy()

    println ">>> Model: " + pretty(mdl, features)
    println ">>> R^2  : " + rr
    println ">>> RMSE : " + rmse
}
