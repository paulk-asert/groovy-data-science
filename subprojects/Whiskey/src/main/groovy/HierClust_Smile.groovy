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
import smile.io.Read
import smile.plot.swing.Dendrogram

import java.awt.Color

import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV
import static smile.math.MathEx.distance

def file = new File(getClass().classLoader.getResource('whiskey.csv').file)
def table = Read.csv(file.toPath(), CSV.withFirstRecordAsHeader())

String[] cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]
def data = table.select(cols).toArray()

int n = data.length
double[][] proximity = new double[n][]
for (i in 0..<n) {
    proximity[i] = new double[i + 1]
    for (j in 0..<i) proximity[i][j] = distance(data[i], data[j])
}
def clusterer = HierarchicalClustering.fit(new WardLinkage(proximity))
//println clusterer.tree
//println clusterer.height

new Dendrogram(clusterer.tree, clusterer.height, Color.BLUE).canvas().with {
    title = "Dendrogram"
    window()
}
