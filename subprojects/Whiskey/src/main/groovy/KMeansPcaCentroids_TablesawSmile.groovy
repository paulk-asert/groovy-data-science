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
import smile.projection.PCA
import tech.tablesaw.api.*
import tech.tablesaw.plotly.api.*

def file = getClass().classLoader.getResource('whiskey.csv').file
def helper = new TablesawHelper(file)
def rows = Table.read().csv(file)

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]
def data = rows.as().doubleMatrix(*cols)

def pca = PCA.fit(data)
def dims = 3
pca.projection = dims
def projected = pca.project(data)
def clusters = KMeans.fit(data, 5)
def labels = clusters.y.collect { "Cluster " + (it + 1) }
rows = rows.addColumns(
    *(0..<dims).collect { idx ->
        DoubleColumn.create("PCA${idx+1}", (0..<data.size()).collect{
            projected[it][idx]
        })
    },
    StringColumn.create("Cluster", labels),
    DoubleColumn.create("Centroid", [10] * labels.size())
)
def centroids = pca.project(clusters.centroids)
def toAdd = rows.emptyCopy(1)
(0..<centroids.size()).each { idx ->
    toAdd[0].setString("Cluster", "Cluster " + (idx+1))
    (1..3).each { toAdd[0].setDouble("PCA" + it, centroids[idx][it-1]) }
    toAdd[0].setDouble("Centroid", 50)
    rows.append(toAdd)
}

def title = "Clusters x Principal Components w/ centroids"
def type = dims == 2 ? ScatterPlot : Scatter3DPlot
helper.show(type.create(title, rows, *(1..dims).collect { "PCA$it" }, "Centroid", "Cluster"), 'KMeansClustersPcaCentroids')
