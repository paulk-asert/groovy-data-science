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

import com.datumbox.framework.applications.nlp.TextClassifier
import com.datumbox.framework.common.Configuration
import com.datumbox.framework.core.common.text.extractors.NgramsExtractor
import com.datumbox.framework.core.machinelearning.MLBuilder
import com.datumbox.framework.core.machinelearning.classification.MultinomialNaiveBayes
import com.datumbox.framework.core.machinelearning.featureselection.ChisquareSelect

def datasets = [
    positive: getClass().classLoader.getResource("rt-polarity.pos").toURI(),
    negative: getClass().classLoader.getResource("rt-polarity.neg").toURI()
]

def trainingParams = new TextClassifier.TrainingParameters(
    numericalScalerTrainingParameters: null,
    featureSelectorTrainingParametersList: [new ChisquareSelect.TrainingParameters()],
    textExtractorParameters: new NgramsExtractor.Parameters(),
    modelerTrainingParameters: new MultinomialNaiveBayes.TrainingParameters()
)

// RandomGenerator.globalSeed = -1L // for repeatable results
def config = Configuration.configuration
TextClassifier classifier = MLBuilder.create(trainingParams, config)
classifier.fit(datasets)
def metrics = classifier.validate(datasets)
println "Classifier Accuracy (using training data): $metrics.accuracy"

// uncomment if you want to save the data
//classifier.save("SentimentAnalysis")

['Datumbox is divine!',
 'Groovy is great fun!',
 'Math can be hard!'].each {
    def r = classifier.predict(it)
    def predicted = r.YPredicted
    def probability = r.YPredictedProbabilities.get(predicted)
    println "Classifing: '$it',  Predicted: $predicted,  Probability: ${ sprintf '%4.2f', probability }"
}

//classifier.delete()
