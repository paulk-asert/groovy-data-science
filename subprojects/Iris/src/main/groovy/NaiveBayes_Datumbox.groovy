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

import com.datumbox.framework.common.Configuration
import com.datumbox.framework.core.common.dataobjects.Dataframe
import com.datumbox.framework.core.machinelearning.MLBuilder
import com.datumbox.framework.core.machinelearning.classification.SoftMaxRegression
import com.datumbox.framework.core.machinelearning.featureselection.PCA
import com.datumbox.framework.core.machinelearning.modelselection.metrics.ClassificationMetrics
import com.datumbox.framework.core.machinelearning.modelselection.splitters.ShuffleSplitter
import com.datumbox.framework.core.machinelearning.preprocessing.MinMaxScaler

import static com.datumbox.framework.common.dataobjects.TypeInference.DataType.CATEGORICAL
import static com.datumbox.framework.common.dataobjects.TypeInference.DataType.NUMERICAL

def file = getClass().classLoader.getResource('iris_data.csv').file as File

def cols = ['Sepal length', 'Sepal width', 'Petal length', 'Petal width']
// RandomGenerator.globalSeed = -1L // for repeatable results
def config = Configuration.configuration
def headers = [*: cols.collectEntries { [it, NUMERICAL] }, Class: CATEGORICAL]
Dataframe data = null
def defaultSeps = [',' as char, '"' as char, "\r\n"]
file.withReader {
    data = Dataframe.Builder.parseCSVFile(it, 'Class', headers, *defaultSeps, null, null, config)
}

def split = new ShuffleSplitter(0.8, 1).split(data).next()
Dataframe training = split.train
Dataframe testing = split.test

def scalerParams = new MinMaxScaler.TrainingParameters()
def scaler = MLBuilder.create(scalerParams, config)
scaler.fit_transform(training)
scaler.save("Class")

def pcaParams = new PCA.TrainingParameters(
        maxDimensions: training.xColumnSize() - 1,
        whitened: false,
        variancePercentageThreshold: 0.99999995
)
PCA pca = MLBuilder.create(pcaParams, config)
pca.fit_transform(training)
pca.save("Class")

def classifierParams = new SoftMaxRegression.TrainingParameters(
        totalIterations: 200,
        learningRate: 0.1
)

SoftMaxRegression classifier = MLBuilder.create(classifierParams, config)
classifier.fit(training)
classifier.save("Class")

scaler.transform(testing)
pca.transform(testing)

classifier.predict(testing)

println "Results:"
testing.entries().each {
    println "Record $it.key - Real Y: $it.value.y, Predicted Y: $it.value.YPredicted"
}

def metrics = new ClassificationMetrics(testing)
println "Classifier Accuracy: $metrics.accuracy"

[scaler, pca, classifier]*.delete()
[training, testing]*.close()
