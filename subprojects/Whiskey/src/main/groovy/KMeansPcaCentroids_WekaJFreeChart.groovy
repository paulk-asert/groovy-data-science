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
//@Grab('nz.ac.waikato.cms.weka:weka-stable:3.8.5')
import weka.attributeSelection.PrincipalComponents
import weka.clusterers.SimpleKMeans
import weka.core.Instance
import weka.core.converters.CSVLoader

import static JFreeChartUtil.*

def file = getClass().classLoader.getResource('whiskey.csv').file as File
def cols = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco', 'Honey',
            'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']

def numClusters = 4
def loader = new CSVLoader(file: file)
def clusterer = new SimpleKMeans(numClusters: numClusters, preserveInstancesOrder: true)
def instances = loader.dataSet
instances.deleteAttributeAt(0) // remove RowID
clusterer.buildClusterer(instances)
println '           ' + cols.join(', ')
def category = categoryDataset()
def xyz = xyzDataset()

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
def zvalues = (0..<transformed.numInstances()).collect{transformed[it].value(2) }
def (zmin, zmax) = [zvalues.min(), zvalues.max()]

clusterer.assignments.eachWithIndex { cnum, idx ->
    clusters[cnum] << instances[idx].stringValue(0)
    x[cnum] << transformed[idx].value(0)
    y[cnum] << transformed[idx].value(1)
    z[cnum] << (transformed[idx].value(2) - zmin + 0.5)/(zmax - zmin) * 2
}

clusters.each { k, v ->
    println "Cluster ${k+1}:"
    println v.join(', ')
    xyz.addSeries("Cluster ${k+1}:", [x[k], y[k], z[k]] as double[][])
}

def spiderPlot = spiderWebPlot(dataset: category)
def spiderChart = chart('Centroids spider plot', spiderPlot)

def xaxis = numberAxis(label: 'PCA1', autoRange: false, lowerBound: -5, upperBound: 10)
def yaxis = numberAxis(label: 'PCA2', autoRange: false, lowerBound: -7, upperBound: 5)
def bubbleChart = chart('PCA bubble plot', xyPlot(xyz, xaxis, yaxis, bubbleRenderer(0.15f)))

SwingUtil.showH(spiderChart, bubbleChart, size: [800, 400],
        title: 'Whiskey clusters: Weka=CSV,KMeans,PCA JFreeChart=plots')
