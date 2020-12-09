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
import org.apache.ignite.Ignition
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction
import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.ml.clustering.kmeans.KMeansTrainer
import org.apache.ignite.ml.dataset.feature.extractor.Vectorizer
import org.apache.ignite.ml.dataset.feature.extractor.impl.DoubleArrayVectorizer
import org.apache.ignite.ml.math.distances.EuclideanDistance
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
  def dataCache = ignite.createCache(new CacheConfiguration<Integer, double[]>(
          name: "TEST_${UUID.randomUUID()}",
          affinity: new RendezvousAffinityFunction(false, 10)))
  (0..<data.length).each { int i -> dataCache.put(i, data[i]) }
  def vectorizer = new DoubleArrayVectorizer().labeled(Vectorizer.LabelCoordinate.FIRST)
  def mdl = trainer.fit(ignite, dataCache, vectorizer)
  println ">>> KMeans centroids"
  println features[1..-1].join(', ')
  mdl.centers.each { c -> println c.all().collect{ sprintf '%.4f', it.get() }.join(', ') }
  dataCache.destroy()
}
