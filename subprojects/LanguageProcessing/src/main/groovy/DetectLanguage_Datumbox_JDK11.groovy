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
        English: getClass().classLoader.getResource("training.language.en.txt").toURI(),
        French: getClass().classLoader.getResource("training.language.fr.txt").toURI(),
        German: getClass().classLoader.getResource("training.language.de.txt").toURI(),
        Spanish: getClass().classLoader.getResource("training.language.es.txt").toURI(),
        Indonesian: getClass().classLoader.getResource("training.language.id.txt").toURI()
]

def trainingParams = new TextClassifier.TrainingParameters(
        numericalScalerTrainingParameters: null,
        featureSelectorTrainingParametersList: [new ChisquareSelect.TrainingParameters()],
        textExtractorParameters: new NgramsExtractor.Parameters(),
        modelerTrainingParameters: new MultinomialNaiveBayes.TrainingParameters()
)

TextClassifier classifier = MLBuilder.create(trainingParams, config)
classifier.fit(datasets)
classifier.save("SentimentAnalysis")

[ 'Bienvenido a Madrid',
  'Bienvenue à Paris',
  'Welcome to London',
  'Willkommen in Berlin',
  'Selamat Datang di Jakarta'
].each {
    def r = classifier.predict(it)
    def predicted = r.YPredicted
    def probability = r.YPredictedProbabilities.get(predicted)
    println "Classifying: '$it',  Predicted: $predicted,  Probability: ${ sprintf '%4.2f', probability }"
}

def metrics = classifier.validate(datasets)
println "Classifier Accuracy (using training data): $metrics.accuracy"

classifier.delete()
