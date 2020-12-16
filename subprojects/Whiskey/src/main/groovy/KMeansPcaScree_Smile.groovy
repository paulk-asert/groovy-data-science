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
import smile.clustering.KMeans
import smile.io.Read
import smile.plot.swing.PlotGrid
import smile.plot.swing.ScatterPlot
import smile.plot.swing.ScreePlot
import smile.projection.PCA

import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

def file = new File(getClass().classLoader.getResource('whiskey.csv').file)
def table = Read.csv(file.toPath(), CSV.withFirstRecordAsHeader())

String[] cols = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco', 'Honey',
                 'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']
def data = table.select(cols).toArray()

def p = 2 // number of dimensions in projection (2 or 3)
def pca = PCA.fit(data)
pca.projection = p
def plots = [new ScreePlot(pca).canvas()]
def projected = pca.project(data)
char mark = '#'
String[] labels = (1..p).collect { "PCA$it" }

(2..6).each { k ->
    println "Processing cluster size $k"
    def clusters = KMeans.fit(data, k)
    plots << ScatterPlot.of(projected, clusters.y, mark).canvas().tap { setAxisLabels(labels) }
}

new PlotGrid(*plots*.panel()).window()
