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

def loader = getClass().classLoader
def datasets = [
    English: loader.getResource("training.language.en.txt").toURI(),
    French: loader.getResource("training.language.fr.txt").toURI(),
    German: loader.getResource("training.language.de.txt").toURI(),
    Spanish: loader.getResource("training.language.es.txt").toURI(),
    Indonesian: loader.getResource("training.language.id.txt").toURI()
]

def trainingParams = new TextClassifier.TrainingParameters(
    numericalScalerTrainingParameters: null,
    featureSelectorTrainingParametersList: [new ChisquareSelect.TrainingParameters()],
    textExtractorParameters: new NgramsExtractor.Parameters(),
    modelerTrainingParameters: new MultinomialNaiveBayes.TrainingParameters()
)

// RandomGenerator.globalSeed = -1L // for repeatable results
def config = Configuration.configuration
def classifier = MLBuilder.create(trainingParams, config)
classifier.fit(datasets)
def metrics = classifier.validate(datasets)
println "Classifier Accuracy (using training data): $metrics.accuracy"

// uncomment to save the model
//classifier.save("LanguageDetection")

// alternative to load a pre-existing model
//def config = Configuration.configuration.tap {
//    storageConfiguration = new InMemoryConfiguration(directory: '/path/to/datumbox-framework-zoo')
//}
//def classifier = MLBuilder.load(TextClassifier, 'LanguageDetection', config)

println 'Classifying                   Predicted   Probability'
[ 'Bienvenido a Madrid', 'Bienvenue à Paris', 'Welcome to London',
  'Willkommen in Berlin', 'Selamat Datang di Jakarta'
].each { txt ->
    def r = classifier.predict(txt)
    def predicted = r.YPredicted
    def probability = r.YPredictedProbabilities.get(predicted)
    println txt.padRight(30) +
        predicted.center(10) +
        sprintf('%6.2f', probability)
}
