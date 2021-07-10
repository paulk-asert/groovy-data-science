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
//@Grab('org.apache.opennlp:opennlp-tools:1.9.3')

import opennlp.tools.doccat.DoccatFactory
import opennlp.tools.doccat.DocumentCategorizerME
import opennlp.tools.doccat.DocumentSample
import opennlp.tools.doccat.DocumentSampleStream
import opennlp.tools.ml.naivebayes.NaiveBayesTrainer
import opennlp.tools.util.CollectionObjectStream
import opennlp.tools.util.ObjectStream
import opennlp.tools.util.TrainingParameters

def datasets = [
        positive: getClass().classLoader.getResource("rt-polarity.pos").file,
        negative: getClass().classLoader.getResource("rt-polarity.neg").file
]
def training = datasets.collect { k, v ->
    new File(v).readLines().collect{"$k $it".toString() }
}.sum()

def variants = [
        Maxent: new TrainingParameters(),
        NaiveBayes: new TrainingParameters((TrainingParameters.CUTOFF_PARAM): '0',
        (TrainingParameters.ALGORITHM_PARAM): NaiveBayesTrainer.NAIVE_BAYES_VALUE)
]
def models = [:]
variants.each{ key, params ->
    def trainingStream = new CollectionObjectStream(training)
    ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(trainingStream)
    println "Training using $key"
    models[key] = DocumentCategorizerME.train('en', sampleStream, params, new DoccatFactory())
}

def sentences = ['Datumbox is divine!',
                 'Groovy is great fun!',
                 'Math can be hard!']
def w = sentences*.size().max()

variants.each { key, params ->
    def categorizer = new DocumentCategorizerME(models[key])
    println "\nAnalyzing using $key"
    sentences.each {
        def result = categorizer.categorize(it.split('[ !]'))
        def category = categorizer.getBestCategory(result)
        def prob = result[categorizer.getIndex(category)]
        println "${it.padRight(w)} $category (${ sprintf '%4.2f', prob})}"
    }
}
