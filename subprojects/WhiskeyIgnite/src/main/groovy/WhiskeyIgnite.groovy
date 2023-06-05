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
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.IgniteConfiguration

//import org.apache.ignite.ml.clustering.gmm.GmmTrainer
import org.apache.ignite.ml.clustering.kmeans.KMeansTrainer
import org.apache.ignite.ml.dataset.feature.extractor.impl.DoubleArrayVectorizer
import org.apache.ignite.ml.math.distances.*
//import smile.feature.extraction.PCA

import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder

//import static JFreeChartUtil.*
import static org.apache.commons.csv.CSVFormat.RFC4180
import static org.apache.ignite.ml.dataset.feature.extractor.Vectorizer.LabelCoordinate.FIRST

var file = getClass().classLoader.getResource('whiskey.csv').file as File
var rows = file.withReader {r -> RFC4180.parse(r).records*.toList() }
var data = rows[1..-1].collect{ it[2..-1]*.toDouble() } as double[][]
var distilleries = rows[1..-1]*.get(1)
var features = rows[0][2..-1]

// configure to all run on local machine but could be a cluster (can be hidden in XML)
var cfg = new IgniteConfiguration(
        peerClassLoadingEnabled: true,
        discoverySpi: new TcpDiscoverySpi(
                ipFinder: new TcpDiscoveryMulticastIpFinder(
                        addresses: ['127.0.0.1:47500..47509']
                )
        )
)

var pretty = this.&sprintf.curry('%.4f')
//var dist = new ManhattanDistance()
var dist = new EuclideanDistance() // or ManhattanDistance
var vectorizer = new DoubleArrayVectorizer().labeled(FIRST)

Ignition.start(cfg).withCloseable { ignite ->
    println ">>> Ignite grid started for data: ${data.size()} rows X ${data[0].size()} cols"
    var dataCache = ignite.createCache(new CacheConfiguration<Integer, double[]>(
          name: "TEST_${UUID.randomUUID()}",
          affinity: new RendezvousAffinityFunction(false, 10)))
    data.indices.each { int i -> dataCache.put(i, data[i]) }
    var trainer = new KMeansTrainer().withDistance(dist).withAmountOfClusters(3)
//    var trainer = new GmmTrainer().withMaxCountOfClusters(5)
    var mdl = trainer.fit(ignite, dataCache, vectorizer)
    println ">>> KMeans centroids:\n${features.join(', ')}"
    var centroids = mdl.centers*.all()
    var cols = centroids.collect{ it*.get() }
    cols.each { c -> println c.collect(pretty).join(', ') }

//    var centroidDataset = categoryDataset()
//    cols.eachWithIndex { nums, i ->
//        nums.eachWithIndex { val, j -> centroidDataset.addValue(val, "Cluster ${i + 1}", features[j]) }
//    }

//    var centroidPlot = spiderWebPlot(dataset: centroidDataset)
//    var centroidChart = chart('Centroid spider plot', centroidPlot)
//    var xyz = xyzDataset()
//    def pca = PCA.fit(data).getProjection(3)

    var clusters = [:].withDefault{ [] }
//    var projected = [:].withDefault{ [] }
    var observationsMap = [:].withDefault{ [:].withDefault{ [] as Set } }
    dataCache.query(new ScanQuery<>()).withCloseable { observations ->
        observations.each { observation ->
            def (k, v) = observation.with{ [getKey(), getValue()] }
            def vector = vectorizer.extractFeatures(k, v)
            int prediction = mdl.predict(vector)
            clusters[prediction] += distilleries[k]
//            projected[prediction] += pca.apply(v)
            v.eachWithIndex{ val, idx ->
                observationsMap[prediction][features[idx]] += val
            }
        }
    }

//    projected.keySet().sort().each { k ->
//        xyz.addSeries("Cluster ${k + 1}:", projected[k].transpose() as double[][])
//    }

    var threshold = 1 // or 2
    clusters.sort{ e -> e.key }.each{ k, v ->
        println "\nCluster ${k+1}: ${v.sort().join(', ')}"
        println "Distinguishing features: " + observationsMap[k]
            .collectEntries{ k1, v1 -> [k1, v1.with{ [it.min() as int, it.max() as int] } ] }
            .findAll{ k2, v2 -> v2[1] - v2[0] <= threshold }
            .collect{ k3, v3 -> "$k3=${v3[0] == v3[1] ? v3[0] : v3[0] + '..' + v3[1]}" }
            .join(', ')
    }

//    var suffix1 = trainer.class.simpleName
//    var suffix2 = trainer instanceof KMeansTrainer ? " $dist.class.simpleName" : ''
//    var title = "Whiskey clusters with Apache Ignite ($suffix1$suffix2)"
//    var xaxis = numberAxis(label: 'PCA1', autoRange: false, lowerBound: -3, upperBound: 7)
//    var yaxis = numberAxis(label: 'PCA2', autoRange: false, lowerBound: -3, upperBound: 5)
//    var bubbleChart = chart('PCA bubble plot', xyPlot(xyz, xaxis, yaxis, bubbleRenderer()))
//    SwingUtil.showH(centroidChart, bubbleChart, size: [800, 400], title: title)

    dataCache.destroy()
}
