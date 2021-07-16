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

import org.tribuo.MutableDataset
import org.tribuo.classification.LabelFactory
import org.tribuo.clustering.kmeans.KMeansTrainer
import org.tribuo.data.csv.CSVLoader

import static org.tribuo.clustering.kmeans.KMeansTrainer.Distance.EUCLIDEAN

def cols = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco', 'Honey',
            'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']

def url = getClass().classLoader.getResource('whiskey.csv').toURI().toURL()
def dataSource = new CSVLoader<>(new LabelFactory()).loadDataSource(url, ['RowID', 'Distillery'] as Set)
def data = new MutableDataset(dataSource)

def trainer = new KMeansTrainer(3, 10, EUCLIDEAN, 1, 1)
def model = trainer.train(data)

model.centroids.eachWithIndex{ centroid, i ->
    println """Cluster $i: ${centroid.collect{
        def v = sprintf '%5.3f', it.value
        "$it.name: $v"
    }.join(',')}"""
}
def clusters = model.predict(data)
def groups = [:].withDefault{ [] }

clusters.eachWithIndex{ prediction, i ->
    groups[prediction.output.ID] << data.getExample(i)
}
groups.keySet().sort().each { group ->
    println "Cluster $group:"
    println groups[group].collect{ it.output.label.replaceAll(/.*Distillery=(\w+)\W.*/,/$1/) }.join(', ')
}
