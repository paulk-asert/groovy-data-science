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
import weka.clusterers.HierarchicalClusterer
import weka.core.converters.CSVLoader
import weka.gui.hierarchyvisualizer.HierarchyVisualizer

import static JFreeChartUtil.*

def file = getClass().classLoader.getResource('whiskey.csv').file as File
def cols = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco', 'Honey',
            'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']

def loader = new CSVLoader(file: file)
def clusterer = new HierarchicalClusterer(options: '-N 4 -L COMPLETE'.split(' '))
def instances = loader.dataSet
def distilleries = instances.collect{ row -> row.stringValue(1) }
instances.deleteAttributeAt(1) // remove Distilleries
instances.deleteAttributeAt(0) // remove RowID

clusterer.buildClusterer(instances)
def numClusters = clusterer.numberOfClusters()

PrincipalComponents pca = new PrincipalComponents(varianceCovered: 0.9, maximumAttributeNames: 3)
pca.buildEvaluator(instances)
//println pca
def transformed = pca.transformedData(instances)
def clusters = (0..<numClusters).collectEntries{ [it, []] }
def (x, y, z) = [[:].withDefault{[]}, [:].withDefault{[]}, [:].withDefault{[]}]
def zvalues = (0..<transformed.numInstances()).collect{transformed[it].value(2) }
def (zmin, zmax) = [zvalues.min(), zvalues.max()]

instances.indices.each { idx ->
    def cnum = clusterer.clusterInstance(instances[idx])
    clusters[cnum] << distilleries[idx]
    x[cnum] << transformed[idx].value(0)
    y[cnum] << transformed[idx].value(1)
    z[cnum] << (transformed[idx].value(2) - zmin + 0.2)/(zmax - zmin) * 1.5
}

def xyz = xyzDataset()
clusters.each { k, v ->
    println "Cluster ${k+1}:"
    println v.join(', ')
    xyz.addSeries("Cluster ${k+1}:", [x[k], y[k], z[k]] as double[][])
}

def hierChart = new HierarchyVisualizer(clusterer.graph())

def xaxis = numberAxis(label: 'PCA1', autoRange: false, lowerBound: -5, upperBound: 10)
def yaxis = numberAxis(label: 'PCA2', autoRange: false, lowerBound: -7, upperBound: 5)
def bubbleChart = chart('PCA bubble plot', xyPlot(xyz, xaxis, yaxis, bubbleRenderer(0.15f)))

SwingUtil.show(hierChart, size: [1200, 400],
        title: 'Whiskey clusters: Weka=CSV,KMeans,PCA JFreeChart=plots')
SwingUtil.show(bubbleChart, size: [600, 600],
        title: 'Whiskey clusters: Weka=CSV,KMeans,PCA JFreeChart=plots')
