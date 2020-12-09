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
//@Grab('org.jfree:jfreechart:1.5.1')
//@Grab('nz.ac.waikato.cms.weka:weka-stable:3.8.4')
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.SpiderWebPlot
import org.jfree.chart.plot.XYPlot
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.DefaultXYZDataset
import weka.attributeSelection.PrincipalComponents
import weka.clusterers.SimpleKMeans
import weka.core.Instance
import weka.core.converters.CSVLoader

import static JFreeChartUtil.bubbleRenderer
import static JFreeChartUtil.chart

def file = getClass().classLoader.getResource('whiskey.csv').file as File
//def file = 'src/main/resources/whiskey.csv'
def cols = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco', 'Honey',
            'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']

def numClusters = 4
def loader = new CSVLoader(file: file)
def clusterer = new SimpleKMeans(numClusters: numClusters, preserveInstancesOrder: true)
def instances = loader.dataSet
instances.deleteAttributeAt(0) // remove RowID
clusterer.buildClusterer(instances)
println '           ' + cols.join(', ')
def category = new DefaultCategoryDataset()
def xyz = new DefaultXYZDataset()

clusterer.clusterCentroids.eachWithIndex{ Instance ctrd, num ->
    print "Cluster ${num+1}: "
    println ((1..cols.size()).collect{ sprintf '%.3f', ctrd.value(it) }.join(', '))
    (1..cols.size()).each { idx ->
        category.addValue(ctrd.value(idx), "Cluster ${num+1}", cols[idx-1]) }
}

PrincipalComponents pca = new PrincipalComponents()
pca.buildEvaluator(instances)
pca.setVarianceCovered(0.9)
pca.setMaximumAttributeNames(3)
//println pca
def transformed = pca.transformedData(instances)
def clusters = (0..<numClusters).collectEntries{ [it, []] }
def (x, y, z) = [[:].withDefault{[]}, [:].withDefault{[]}, [:].withDefault{[]}]
def zvalues = (0..<transformed.numInstances()).collect{transformed.get(it).value(2) }
def (zmin, zmax) = [zvalues.min(), zvalues.max()]

clusterer.assignments.eachWithIndex { cnum, idx ->
    clusters[cnum] << instances.get(idx).stringValue(0)
    x[cnum] << transformed.get(idx).value(0)
    y[cnum] << transformed.get(idx).value(1)
    z[cnum] << (transformed.get(idx).value(2) - zmin + 0.5)/(zmax - zmin) * 2
}

clusters.each { k, v ->
    println "Cluster ${k+1}:"
    println v.join(', ')
    xyz.addSeries("Cluster ${k+1}:", [x[k], y[k], z[k]] as double[][])
}

def spiderPlot = new SpiderWebPlot(dataset: category)
def spiderChart = chart('Centroids spider plot', spiderPlot)

def xaxis = new NumberAxis(label: 'PCA1', autoRange: false, lowerBound: -5, upperBound: 10)
def yaxis = new NumberAxis(label: 'PCA2', autoRange: false, lowerBound: -7, upperBound: 5)
def bubbleChart = chart('PCA bubble plot', new XYPlot(xyz, xaxis, yaxis, bubbleRenderer(0.2f)))

SwingUtil.showH(spiderChart, bubbleChart, size: [800, 400],
        title: 'Whiskey clusters: Weka=CSV,KMeans,PCA JFreeChart=plots')
