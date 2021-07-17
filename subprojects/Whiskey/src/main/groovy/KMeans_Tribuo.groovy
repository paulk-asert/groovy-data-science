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
import org.tribuo.data.columnar.RowProcessor
import org.tribuo.data.columnar.extractors.IdentityExtractor
import org.tribuo.data.columnar.processors.field.DoubleFieldProcessor
import org.tribuo.data.columnar.processors.response.EmptyResponseProcessor
import org.tribuo.data.csv.CSVDataSource

import static org.tribuo.clustering.kmeans.KMeansTrainer.Distance.EUCLIDEAN

def cols = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco', 'Honey',
            'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']
def fieldProcessors = cols.collectEntries{ [it, new DoubleFieldProcessor(it)] }
def responseProcessor = new EmptyResponseProcessor(new LabelFactory())
def metadataExtractors = [new IdentityExtractor('Distillery')]
def rowProcessor = new RowProcessor(metadataExtractors, responseProcessor, fieldProcessors)

def uri = getClass().classLoader.getResource('whiskey.csv').toURI()
def dataSource = new CSVDataSource(uri, rowProcessor, false)

def data = new MutableDataset(dataSource)

def trainer = new KMeansTrainer(3, 10, EUCLIDEAN, 1, 1)
def model = trainer.train(data)

def centroids = model.centroids.indexed().collectEntries{ i,centroid ->
    [i, centroid.collect{"${it.name - '@value'}: ${sprintf '%5.3f', it.value}" }]
}

def clusters = model.predict(data)
def groups = [:].withDefault{ [] }
clusters.eachWithIndex{ prediction, i ->
    groups[prediction.output.ID] << data.getExample(i)
}

groups.keySet().sort().each { group ->
    println "\nCluster $group:"
    println "Centroid: ${centroids[group].join(', ')}"
    println 'Distilleries: ' + groups[group].collect{ it.getMetadataValue('Distillery').get() }.join(', ')
}
