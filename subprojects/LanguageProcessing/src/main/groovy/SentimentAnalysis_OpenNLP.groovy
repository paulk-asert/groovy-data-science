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
import opennlp.tools.doccat.DocumentSampleStream
import opennlp.tools.util.CollectionObjectStream
import opennlp.tools.util.TrainingParameters

import static opennlp.tools.ml.naivebayes.NaiveBayesTrainer.NAIVE_BAYES_VALUE
import static opennlp.tools.util.TrainingParameters.ALGORITHM_PARAM
import static opennlp.tools.util.TrainingParameters.CUTOFF_PARAM

def datasets = [
        positive: getClass().classLoader.getResource("rt-polarity.pos").file,
        negative: getClass().classLoader.getResource("rt-polarity.neg").file
]

// OpenNLP is expecting one dataset with each line containing the category as the first term
def trainingCollection = datasets.collect { k, v ->
    new File(v).readLines().collect{"$k $it".toString() }
}.sum()

def variants = [
        Maxent    : new TrainingParameters(),
        NaiveBayes: new TrainingParameters((CUTOFF_PARAM): '0', (ALGORITHM_PARAM): NAIVE_BAYES_VALUE)
]
def models = [:]

variants.each{ key, trainingParams ->
    def trainingStream = new CollectionObjectStream(trainingCollection)
    def sampleStream = new DocumentSampleStream(trainingStream)
    println "\nTraining using $key"
    models[key] = DocumentCategorizerME.train('en', sampleStream, trainingParams, new DoccatFactory())
}

def sentences = ['OpenNLP is fantastic!',
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
