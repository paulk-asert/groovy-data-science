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
import org.encog.ml.data.basic.BasicMLData
import org.encog.ml.data.basic.BasicMLDataSet
import org.encog.ml.kmeans.KMeansClustering
import org.encog.util.csv.CSVFormat
import org.encog.util.csv.ReadCSV

def cols = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco', 'Honey',
            'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']

def file = getClass().classLoader.getResource('whiskey.csv').file
def csv = new ReadCSV(file, true, CSVFormat.EG_FORMAT)
def set = new BasicMLDataSet()
while (csv.next()) {
    set.add(new BasicMLData(cols.collect{col -> csv.getDouble(col) } as double[]))
}

def clusterer = new KMeansClustering(3, set)
clusterer.iteration(100)

clusterer.clusters.eachWithIndex { cluster, idx ->
    println "Cluster $idx: "
    cluster.createDataSet().each {
        println it.inputArray
    }
}
