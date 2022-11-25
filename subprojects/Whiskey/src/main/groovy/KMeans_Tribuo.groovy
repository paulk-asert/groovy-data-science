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
import org.tribuo.math.distance.L2Distance

var cols = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco', 'Honey',
            'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']
var fieldProcessors = cols.collect{ new DoubleFieldProcessor(it) }
var rowProcessor = new RowProcessor.Builder()
        .addMetadataExtractor(new IdentityExtractor('Distillery'))
        .setFieldProcessors(fieldProcessors)
        .build(new EmptyResponseProcessor(new LabelFactory()))

var uri = getClass().classLoader.getResource('whiskey.csv').toURI()
var dataSource = new CSVDataSource(uri, rowProcessor, false)

var data = new MutableDataset(dataSource)

var trainer = new KMeansTrainer(3, 10, new L2Distance(), 1, 1L)
var model = trainer.train(data)

var centroids = model.centroids.indexed().collectEntries{ i,centroid ->
    [i, centroid.collect{"${it.name - '@value'}: ${sprintf '%5.3f', it.value}" }]
}

var clusters = model.predict(data)
var groups = [:].withDefault{ [] }
clusters.eachWithIndex{ prediction, i ->
    groups[prediction.output.ID] << data.getExample(i)
}

groups.keySet().sort().each { group ->
    println "\nCluster $group:"
    println "Centroid: ${centroids[group].join(', ')}"
    println 'Distilleries: ' + groups[group].collect{
        it.getMetadataValue('Distillery').get()
    }.join(', ')
}
