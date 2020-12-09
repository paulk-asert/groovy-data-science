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
import java.awt.Color
import smile.clustering.KMeans
import smile.plot.PlotCanvas
import smile.plot.PlotPanel
import smile.plot.ScatterPlot
import smile.projection.PCA
import tech.tablesaw.api.DoubleColumn
import tech.tablesaw.api.Table
import static java.awt.Color.*

def file = getClass().classLoader.getResource('whiskey.csv').file
def table = Table.read().csv(file)
//def table = Table.read().csv('whiskey.csv')
table = table.removeColumns(0)

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]
def data = table.as().doubleMatrix(*cols)

def pca = new PCA(data)
pca.projection = 2
def plots = [PlotCanvas.screeplot(pca)]
def projected = pca.project(data)
table = table.addColumns(
        *(1..2).collect { idx ->
            DoubleColumn.create("PCA$idx", (0..<data.size()).collect { projected[it][idx - 1] })
        }
)

def colors = [RED, BLUE, GREEN, ORANGE, MAGENTA, GRAY]
def symbols = ['*', 'Q', '#', 'Q', '*', '#']
(2..6).each { k ->
    def clusterer = new KMeans(data, k)
    double[][] components = table.as().doubleMatrix('PCA1', 'PCA2')
    plots << ScatterPlot.plot(components, clusterer.clusterLabel,
            symbols[0..<k] as char[], colors[0..<k] as Color[])
}

SwingUtil.show(size: [1200, 900], new PlotPanel(*plots))
