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
import smile.clustering.HierarchicalClustering
import smile.clustering.linkage.CompleteLinkage
import smile.feature.Standardizer
import smile.io.Read
import smile.plot.swing.Dendrogram
import smile.plot.swing.Label
import smile.plot.swing.Palette
import smile.plot.swing.PlotGrid
import smile.plot.swing.ScatterPlot
import smile.projection.PCA

import java.awt.Color
import java.awt.Font

import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

def file = new File(getClass().classLoader.getResource('beer.csv').file)
def table = Read.csv(file.toPath(), CSV.withFirstRecordAsHeader())

String[] cols = ['alcohol','calories','sodium','cost']
def data = table.select(cols).toArray()
def scaler = Standardizer.fit(data)
data = scaler.transform(data)
def distilleries = table.column('name').toStringArray()
def ninetyDeg = 1.57 // radians
def FOREST_GREEN = new Color(0X808000)

def clusters = HierarchicalClustering.fit(CompleteLinkage.of(data))
//println clusters.tree
//println clusters.height
def partitions = clusters.partition(3)

// little trick to work out cluster colors
def colorMap = new LinkedHashSet(partitions.toList()).toList().reverse().indexed().collectEntries { k, v -> [v, Palette.COLORS[k]] }
Font font = new Font("BitStream Vera Sans", Font.PLAIN, 12)

def dendrogram = new Dendrogram(clusters.tree, clusters.height, FOREST_GREEN).canvas().tap {
    title = 'Beer Dendrogram'
    setAxisLabels('Names', 'Similarity')
    def lb = lowerBounds
    setBound([lb[0] - 1, lb[1] - 20] as double[], upperBounds)
    distilleries.eachWithIndex { String label, int i ->
        add(new Label(label, [i, -1] as double[], 0, 0, ninetyDeg, font, colorMap[partitions[i]]))
    }
}.panel()

def pca = PCA.fit(data)
pca.projection = 2
def projected = pca.project(data)

char mark = '#'
def scatter = ScatterPlot.of(projected, partitions, mark).canvas().tap {
    title = 'Clustered by dendrogram partitions'
    setAxisLabels('PCA1', 'PCA2')
}.panel()

new PlotGrid(dendrogram, scatter).window()
