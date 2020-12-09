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
import smile.clustering.linkage.WardLinkage
import smile.math.Math
import smile.plot.Dendrogram
import tech.tablesaw.api.Table

def file = getClass().classLoader.getResource('whiskey.csv').file
def rows = Table.read().csv(file)
//Table rows = Table.read().csv('whiskey.csv')

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]
def data = rows.as().doubleMatrix(*cols)

int n = data.length
double[][] proximity = new double[n][]
for (i in 0..<n) {
    proximity[i] = new double[i + 1]
    for (j in 0..<i) proximity[i][j] = Math.distance(data[i], data[j])
}
def clusterer = new HierarchicalClustering(new WardLinkage(proximity))
//println clusterer.tree
//println clusterer.height
def plot = Dendrogram.plot(clusterer.tree, clusterer.height)
plot.title = "Dendrogram"

SwingUtil.show(plot)
