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
import smile.plot.*
import smile.projection.PCA
import smile.vq.SOM
import tech.tablesaw.api.*
import static java.awt.Color.*

def file = getClass().classLoader.getResource('whiskey.csv').file
def table = Table.read().csv(file)
//def table = Table.read().csv('whiskey.csv')

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]
def data = table.as().doubleMatrix(*cols)
def som = new SOM(data, 40)
def k = 3
def clusters = som.partition(k)
def byName = { table.row(it.intValue()).getText('Distillery') }
(0..<k).each { idx ->
    println "Cluster $idx: " + clusters.findIndexValues { it == idx }.collect(byName).join(', ')
}

def hexmap = Hexmap.plot(som.umatrix(), Palette.heat(256))
def pca = new PCA(data)
pca.projection = 2
def projected = pca.project(data)
table = table.addColumns(
        *(1..2).collect { idx ->
            DoubleColumn.create("PCA$idx", (0..<data.size()).collect { projected[it][idx - 1] })
        }
)

Color[] colors = [RED, BLUE, GREEN]
char[] symbols = ['*', 'Q', '#']
double[][] components = table.as().doubleMatrix('PCA1', 'PCA2')
def scatter = ScatterPlot.plot(components, clusters, symbols, colors)

SwingUtil.show(new PlotPanel(scatter, hexmap))
