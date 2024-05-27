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
import org.apache.commons.math4.legacy.linear.EigenDecomposition
import org.apache.commons.math4.legacy.linear.MatrixUtils
import org.apache.commons.math4.legacy.ml.clustering.DoublePoint
import org.apache.commons.math4.legacy.ml.clustering.KMeansPlusPlusClusterer
import org.apache.commons.math4.legacy.stat.correlation.Covariance

import static JFreeChartUtil.*
import static org.apache.commons.csv.CSVFormat.RFC4180
import static org.apache.commons.math4.legacy.stat.StatUtils.sumSq

var file = getClass().classLoader.getResource('whiskey.csv').file as File
var builder = RFC4180.builder().build()
var records = file.withReader { r -> builder.parse(r).records*.toList() }
var features = records[0][2..-1]
var data = records[1..-1].collect{ new DoublePoint(it[2..-1] as int[]) }
var distilleries = records[1..-1]*.get(1)

var clusterer = new KMeansPlusPlusClusterer(4)
Map<Integer, List> clusterPts = [:]
var clusters = clusterer.cluster(data)
println features.join(', ')
var centroids = categoryDataset()
clusters.eachWithIndex { ctrd, num ->
    var cpt = ctrd.center.point
    clusterPts[num] = ctrd.points.collect { pt -> data.point.findIndexOf { it == pt.point } }
    println cpt.collect { sprintf '%.3f', it }.join(', ')
    cpt.eachWithIndex { val, idx -> centroids.addValue(val, "Cluster ${num + 1}", features[idx]) }
}

println "\n${features.join(', ')}, Medoid"
var medoids = categoryDataset()
clusters.eachWithIndex { ctrd, num ->
    var cpt = ctrd.center.point
    var closest = ctrd.points.min { pt ->
        sumSq((0..<cpt.size()).collect { cpt[it] - pt.point[it] } as double[])
    }
    var medoidIdx = data.findIndexOf { row -> row.point == closest.point }
    println data[medoidIdx].point.collect { sprintf '%.3f', it }.join(', ') + ", ${distilleries[medoidIdx]}"
    data[medoidIdx].point.eachWithIndex { val, idx -> medoids.addValue(val, distilleries[medoidIdx], features[idx]) }
}

var centroidPlot = spiderWebPlot(dataset: centroids)
var centroidChart = chart('Centroid spider plot', centroidPlot)

var medoidPlot = spiderWebPlot(dataset: medoids)
var medoidChart = chart('Medoid spider plot', medoidPlot)

var pointsArray = data*.point as double[][]
var mean = (0..<pointsArray[0].length).collect { col ->
    (0..<pointsArray.length).collect { row ->
        pointsArray[row][col]
    }.average()
} as double[]
(0..<pointsArray[0].length).collect { col ->
    (0..<pointsArray.length).collect { row ->
        pointsArray[row][col] -= mean[col]
    }.average()
}
var pointsMatrix = MatrixUtils.createRealMatrix(pointsArray)

// calculate PCA by hand: create covariance matrix of points, then find eigen vectors
// see https://stats.stackexchange.com/questions/2691/making-sense-of-principal-component-analysis-eigenvectors-eigenvalues

var covariance = new Covariance(pointsMatrix)
var covarianceMatrix = covariance.covarianceMatrix
var decomposition = new EigenDecomposition(covarianceMatrix)
double[] eigenValues = decomposition.realEigenvalues
var k = 3
var components = MatrixUtils.createRealMatrix(eigenValues.length, k)
for (int i = 0; i < k; i++) {
    for (int j = 0; j < eigenValues.length; j++) {
        components.setEntry(j, i, decomposition.getEigenvector(i).getEntry(j))
    }
}

var xyz = xyzDataset()
var projected = pointsMatrix.multiply(components).data

clusterPts.each { num, dists ->
    double[][] series = dists.collect { projected[it] }.transpose()
    xyz.addSeries("Cluster ${num + 1}:", series)
}

var xaxis = numberAxis(label: 'PCA1', autoRange: false, lowerBound: -7, upperBound: 3)
var yaxis = numberAxis(label: 'PCA2', autoRange: false, lowerBound: -5, upperBound: 3)
var bubbleChart = chart('PCA bubble plot', xyPlot(xyz, xaxis, yaxis, bubbleRenderer()))

SwingUtil.showH(centroidChart, medoidChart, bubbleChart, size: [1000, 400],
    title: 'Whiskey clusters: CSV=commons-csv kmeans,PCA=commons-math plot=jfreechart')
