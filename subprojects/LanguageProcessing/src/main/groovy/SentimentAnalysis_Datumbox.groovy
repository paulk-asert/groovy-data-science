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

// RandomGenerator.globalSeed = -1L // for repeatable results
def config = Configuration.configuration

def datasets = [
        positive: getClass().classLoader.getResource("rt-polarity.pos").toURI(),
        negative: getClass().classLoader.getResource("rt-polarity.neg").toURI()
]

TextClassifier.TrainingParameters trainingParams = new TextClassifier.TrainingParameters(
        numericalScalerTrainingParameters: null,
        featureSelectorTrainingParametersList: [new ChisquareSelect.TrainingParameters()],
        textExtractorParameters: new NgramsExtractor.Parameters(),
        modelerTrainingParameters: new MultinomialNaiveBayes.TrainingParameters()
)

TextClassifier textClassifier = MLBuilder.create(trainingParams, config)
textClassifier.fit(datasets)
textClassifier.save("SentimentAnalysis")

['Datumbox is amazing!', 'Groovy is great!', 'Math can be hard.'].each {
    def r = textClassifier.predict(it)
    def predicted = r.YPredicted
    def probability = r.YPredictedProbabilities.get(predicted)
    println "Classifing: '$it',  Predicted: $predicted,  Probability: ${ sprintf '%4.2f', probability }"
}

def metrics = textClassifier.validate(datasets)
println "Classifier Accuracy: $metrics.accuracy"

textClassifier.delete()
