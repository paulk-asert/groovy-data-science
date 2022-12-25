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

import org.hipparchus.linear.EigenDecomposition
import org.hipparchus.linear.MatrixUtils
import org.hipparchus.clustering.DoublePoint
import org.hipparchus.clustering.KMeansPlusPlusClusterer
import org.hipparchus.stat.correlation.Covariance
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.SpiderWebPlot
import org.jfree.chart.plot.XYPlot
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.DefaultXYZDataset

import static JFreeChartUtil.bubbleRenderer
import static JFreeChartUtil.chart
import static org.apache.commons.csv.CSVFormat.RFC4180
import static org.hipparchus.stat.StatUtils.sumSq

var file = getClass().classLoader.getResource('whiskey.csv').file as File
var builder = RFC4180.builder().setHeader().setSkipHeaderRecord(true).build()
var rows = file.withReader {r -> builder.parse(r).records }

var cols = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco', 'Honey',
            'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']

List<DoublePoint> data = []
List<String> distilleries = []
Map<Integer, List> clusterPts = [:]
rows.each{ row ->
    data << new DoublePoint(cols.collect{ col -> row[col] } as int[])
    distilleries << row.Distillery
}

var clusterer = new KMeansPlusPlusClusterer(4)
var clusters = clusterer.cluster(data)
println cols.join(', ')
var centroids = new DefaultCategoryDataset()
clusters.eachWithIndex{ ctrd, num ->
    var cpt = ctrd.center.point
    clusterPts[num] = ctrd.points.collect{ pt -> data.point.findIndexOf{ it == pt.point } }
    println cpt.collect{ sprintf '%.3f', it }.join(', ')
    cpt.eachWithIndex { val, idx -> centroids.addValue(val, "Cluster ${num+1}", cols[idx]) }
}

println "\n${cols.join(', ')}, Medoid"
var medoids = new DefaultCategoryDataset()
clusters.eachWithIndex{ ctrd, num ->
    var cpt = ctrd.center.point
    var closest = ctrd.points.min{ pt ->
        sumSq((0..<cpt.size()).collect{ cpt[it] - pt.point[it] } as double[])
    }
    var medoidIdx = data.findIndexOf{ row -> row.point == closest.point }
    println data[medoidIdx].point.collect{ sprintf '%.3f', it }.join(', ') + ", ${distilleries[medoidIdx]}"
    data[medoidIdx].point.eachWithIndex { val, idx -> medoids.addValue(val, distilleries[medoidIdx], cols[idx]) }
}

var centroidPlot = new SpiderWebPlot(dataset: centroids)
var centroidChart = chart('Centroid spider plot', centroidPlot)

var medoidPlot = new SpiderWebPlot(dataset: medoids)
var medoidChart = chart('Medoid spider plot', medoidPlot)

var points = data*.point as double[][]
var mean = points.transpose()*.average()
(0..<points[0].length).collect{col ->
    (0..<points.length).collect{ row ->
        points[row][col] -= mean[col]
    }.average()
}
var realMatrix = MatrixUtils.createRealMatrix(points)

// calculate PCA by hand: create covariance matrix of points, then find eigen vectors
// see https://stats.stackexchange.com/questions/2691/making-sense-of-principal-component-analysis-eigenvectors-eigenvalues

var covariance = new Covariance(realMatrix)
var covarianceMatrix = covariance.covarianceMatrix
var ed = new EigenDecomposition(covarianceMatrix)
double[] eigenValues = ed.realEigenvalues
var k = clusterer.k
var principalComponents = MatrixUtils.createRealMatrix(eigenValues.length, k)
for (int i = 0; i < k; i++) {
    for (int j = 0; j < eigenValues.length; j++) {
        principalComponents.setEntry(j, i, ed.getEigenvector(i).getEntry(j))
    }
}

var xyz = new DefaultXYZDataset()
var transformed = realMatrix * principalComponents

clusterPts.each{ num, v ->
    def (x, y, z) = [[], [], []]
    v.each { idx ->
        x << -transformed.getEntry(idx, 0)
        y << transformed.getEntry(idx, 1)
        z << -(transformed.getEntry(idx, 2))// - zmin)/(zmax - zmin)*2
    }
    xyz.addSeries("Cluster ${num+1}:", [x, y, z] as double[][])
}

var xaxis = new NumberAxis(label: 'PCA1', autoRange: false, lowerBound: -3.5, upperBound: 7)
var yaxis = new NumberAxis(label: 'PCA2', autoRange: false, lowerBound: -6, upperBound: 4)
var bubbleChart = chart('PCA bubble plot', new XYPlot(xyz, xaxis, yaxis, bubbleRenderer()))

SwingUtil.showH(centroidChart, medoidChart, bubbleChart, size: [1000, 400],
        title: 'Whiskey clusters: CSV=commons-csv kmeans,PCA=hipparchus plot=jfreechart')
