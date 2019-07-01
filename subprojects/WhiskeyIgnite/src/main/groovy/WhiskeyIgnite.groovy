import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.ml.clustering.kmeans.KMeansTrainer
import org.apache.ignite.ml.dataset.feature.extractor.Vectorizer
import org.apache.ignite.ml.dataset.feature.extractor.impl.DoubleArrayVectorizer
import org.apache.ignite.ml.math.distances.EuclideanDistance
import org.apache.ignite.ml.util.SandboxMLCache
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder
import tech.tablesaw.api.Table

def file = getClass().classLoader.getResource('whiskey.csv').file
def rows = Table.read().csv(file)
//def rows = Table.read().csv('whiskey.csv')
String[] features = [
  'Distillery', 'Body', 'Sweetness',
  'Smoky', 'Medicinal', 'Tobacco',
  'Honey', 'Spicy', 'Winey', 'Nutty',
  'Malty', 'Fruity', 'Floral'
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

Ignition.start(cfg).withCloseable { ignite ->
  println ">>> Ignite grid started for data: ${data.size()} rows X ${data[0].size()} cols"
  def trainer = new KMeansTrainer().withDistance(new EuclideanDistance()).withAmountOfClusters(5)
  def dataCache = new SandboxMLCache(ignite).fillCacheWith(data)
  def vectorizer = new DoubleArrayVectorizer().labeled(Vectorizer.LabelCoordinate.FIRST)
  def mdl = trainer.fit(ignite, dataCache, vectorizer)
  println ">>> KMeans centroids"
  println features[1..-1].join(', ')
  mdl.centers.each { c -> println c.all().collect{ sprintf '%.4f', it.get() }.join(', ') }
  dataCache.destroy()
}
