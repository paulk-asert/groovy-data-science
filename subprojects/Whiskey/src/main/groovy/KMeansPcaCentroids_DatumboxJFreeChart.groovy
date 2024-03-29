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
//@Grab('com.datumbox:datumbox-framework-lib:0.8.2')
import com.datumbox.framework.core.common.dataobjects.Dataframe
import com.datumbox.framework.common.Configuration
import com.datumbox.framework.core.machinelearning.MLBuilder
import com.datumbox.framework.core.machinelearning.clustering.Kmeans
import com.datumbox.framework.core.machinelearning.featureselection.PCA

import static JFreeChartUtil.*
import static com.datumbox.framework.common.dataobjects.TypeInference.DataType.NUMERICAL
import static com.datumbox.framework.common.dataobjects.TypeInference.DataType.CATEGORICAL
import static com.datumbox.framework.core.machinelearning.clustering.Kmeans.TrainingParameters.Distance.EUCLIDIAN
import static com.datumbox.framework.core.machinelearning.clustering.Kmeans.TrainingParameters.Initialization.FORGY

def cols = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco', 'Honey',
            'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']
def k = 4

def config = Configuration.configuration
def headers = [*:cols.collectEntries{[it, NUMERICAL] }, Distillery: CATEGORICAL]
Dataframe df = null
def defaultSeps = [',' as char, '"' as char, "\r\n"]
def filename = getClass().classLoader.getResource('whiskey.csv').file
new File(filename).withReader {
    df = Dataframe.Builder.parseCSVFile(it, 'Distillery', headers, *defaultSeps, null, null, config)
}

def trainParams = new Kmeans.TrainingParameters(k: k, maxIterations: 100,
        initializationMethod: FORGY, distanceMethod: EUCLIDIAN, weighted: false)

Kmeans clusterer = MLBuilder.create(trainParams, config)
clusterer.fit(df)
clusterer.predict(df)
def cl = clusterer.clusters
def centroids = categoryDataset()
println 'Centroids ' + cols.join(', ')
cl.each { idx, v ->
    def ctrd = v.centroid.x
    println "Cluster$idx: " + cols.collect{sprintf '%.3f', ctrd.get(it) }.join(', ')
    cols.each { col ->
        centroids.addValue(ctrd.get(col), "Cluster $idx", col)
    }
}

def centroidChart = chart('Centroid spider plot', spiderWebPlot(dataset: centroids))

def pcaParams = new PCA.TrainingParameters(whitened: true, maxDimensions: k)
PCA featureSelector = MLBuilder.create(pcaParams, config)
featureSelector.fit_transform(df)
featureSelector.close()

def xyz = xyzDataset()
def clusters = [:].withDefault{[]}
def (xs, ys, zs) = [[:].withDefault{[]}, [:].withDefault{[]}, [:].withDefault{[]}]
def zmin = df.entries().collect{it.value.x.get(2) }.min()
def zmax = df.entries().collect{it.value.x.get(2) }.max()

df.entries().each{ e ->
    def idx = e.value.YPredicted
    clusters["Cluster$idx"] << e.value.y
    xs[idx] << -e.value.x.get(0)
    ys[idx] << e.value.x.get(1)
    zs[idx] << 0.5 + (e.value.x.get(2) - zmin)/(zmax - zmin) // normalize
}
df.delete()

(0..<k).each{
    xyz.addSeries("Cluster $it:", [xs[it], ys[it], zs[it]] as double[][])
}

println clusters
        .collectEntries{ e -> [e.key, e.value.join(', ')] }
        .sort{e -> e.key }
        .collect{ idx, v -> "$idx: $v" }.join('\n')

def xaxis = numberAxis(label: 'PCA1', autoRange: false, lowerBound: -6, upperBound: 10)
def yaxis = numberAxis(label: 'PCA2', autoRange: false, lowerBound: -9, upperBound: 0)
def bubbleChart = chart('PCA bubble plot', xyPlot(xyz, xaxis, yaxis, bubbleRenderer()))

SwingUtil.show(centroidChart, bubbleChart,
        size: [600, 900],
        title: 'Whiskey clusters: CSV,kmeans,PCA=datumbox plot=jfreechart'
)
