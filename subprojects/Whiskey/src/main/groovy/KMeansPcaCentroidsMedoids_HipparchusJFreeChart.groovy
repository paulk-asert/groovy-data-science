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
import org.hipparchus.clustering.DoublePoint
import org.hipparchus.clustering.KMeansPlusPlusClusterer

import static JFreeChartUtil.*
import static org.apache.commons.csv.CSVFormat.RFC4180
import static org.hipparchus.stat.StatUtils.sumSq

var file = getClass().classLoader.getResource('whiskey.csv').file as File
var builder = RFC4180.builder().setHeader().setSkipHeaderRecord(true).build()
var rows = file.withReader {r -> builder.parse(r).records }

var cols = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco', 'Honey',
            'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']

List<String> distilleries = rows*.Distillery
List<DoublePoint> data = rows.collect { new DoublePoint(cols.collect { col -> it[col] } as int[]) }
Map<Integer, List> clusterPts = [:]

var clusterer = new KMeansPlusPlusClusterer(4)
var clusters = clusterer.cluster(data)
println cols.join(', ')
var centroids = categoryDataset()
clusters.eachWithIndex{ ctrd, num ->
    var cpt = ctrd.center.point
    clusterPts[num] = ctrd.points.collect{ pt -> data.point.findIndexOf{ it == pt.point } }
    println cpt.collect{ sprintf '%.3f', it }.join(', ')
    cpt.eachWithIndex { val, idx -> centroids.addValue(val, "Cluster ${num+1}", cols[idx]) }
}

println "\n${cols.join(', ')}, Medoid"
var medoids = categoryDataset()
clusters.eachWithIndex{ ctrd, num ->
    var cpt = ctrd.center.point
    var closest = ctrd.points.min{ pt ->
        sumSq((0..<cpt.size()).collect{ cpt[it] - pt.point[it] } as double[])
    }
    var medoidIdx = data.findIndexOf{ row -> row.point == closest.point }
    println data[medoidIdx].point.collect{ sprintf '%.3f', it }.join(', ') + ", ${distilleries[medoidIdx]}"
    data[medoidIdx].point.eachWithIndex { val, idx -> medoids.addValue(val, distilleries[medoidIdx], cols[idx]) }
}

var centroidPlot = spiderWebPlot(dataset: centroids)
var centroidChart = chart('Centroid spider plot', centroidPlot)

var medoidPlot = spiderWebPlot(dataset: medoids)
var medoidChart = chart('Medoid spider plot', medoidPlot)

var pca = new PCA(3)
var projected = pca.fitAndTransform(data*.point as double[][])

var xyz = xyzDataset()
clusterPts.each { num, dists ->
    double[][] series = dists.collect { projected[it] }.transpose()
    xyz.addSeries("Cluster ${num + 1}:", series)
}

var xaxis = numberAxis(label: 'PCA1', autoRange: false, lowerBound: -7, upperBound: 3)
var yaxis = numberAxis(label: 'PCA2', autoRange: false, lowerBound: -5, upperBound: 3)
var bubbleChart = chart('PCA bubble plot', xyPlot(xyz, xaxis, yaxis, bubbleRenderer()))

SwingUtil.showH(centroidChart, medoidChart, bubbleChart, size: [1000, 400],
        title: 'Whiskey clusters: CSV=commons-csv kmeans,PCA=hipparchus plot=jfreechart')
