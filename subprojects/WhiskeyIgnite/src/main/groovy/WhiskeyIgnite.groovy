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
import org.apache.ignite.ml.dataset.feature.extractor.impl.DoubleArrayVectorizer
import org.apache.ignite.ml.math.distances.EuclideanDistance
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder

import static org.apache.commons.csv.CSVFormat.RFC4180
import static org.apache.ignite.ml.dataset.feature.extractor.Vectorizer.LabelCoordinate.FIRST

var file = getClass().classLoader.getResource('whiskey.csv').file as File
var rows = file.withReader {r -> RFC4180.parse(r).records*.toList() }
var data = rows[1..-1].collect{ it[2..-1]*.toDouble() } as double[][]

// configure to all run on local machine but could be a cluster (can be hidden in XML)
var cfg = new IgniteConfiguration(
        peerClassLoadingEnabled: true,
        discoverySpi: new TcpDiscoverySpi(
                ipFinder: new TcpDiscoveryMulticastIpFinder(
                        addresses: ['127.0.0.1:47500..47509']
                )
        )
)

var features = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco',
                     'Honey', 'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']
var pretty = this.&sprintf.curry('%.4f')
var dist = new EuclideanDistance()
var vectorizer = new DoubleArrayVectorizer().labeled(FIRST)

Ignition.start(cfg).withCloseable { ignite ->
    println ">>> Ignite grid started for data: ${data.size()} rows X ${data[0].size()} cols"
    var dataCache = ignite.createCache(new CacheConfiguration<Integer, double[]>(
          name: "TEST_${UUID.randomUUID()}",
          affinity: new RendezvousAffinityFunction(false, 10)))
    data.indices.each { int i -> dataCache.put(i, data[i]) }
    var trainer = new KMeansTrainer().withDistance(dist).withAmountOfClusters(5)
    var mdl = trainer.fit(ignite, dataCache, vectorizer)
    println ">>> KMeans centroids:\n${features.join(', ')}"
    var centroids = mdl.centers*.all()
    centroids.each { c -> println c*.get().collect(pretty).join(', ') }
    dataCache.destroy()
}
