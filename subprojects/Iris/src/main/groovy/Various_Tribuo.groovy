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
import org.tribuo.classification.dtree.CARTClassificationTrainer
import org.tribuo.classification.evaluation.LabelEvaluator
import org.tribuo.classification.liblinear.LibLinearClassificationTrainer
import org.tribuo.classification.libsvm.LibSVMClassificationTrainer
import org.tribuo.classification.libsvm.SVMClassificationType
import org.tribuo.classification.mnb.MultinomialNaiveBayesTrainer
import org.tribuo.classification.sgd.linear.LogisticRegressionTrainer
import org.tribuo.classification.xgboost.XGBoostClassificationTrainer
import org.tribuo.common.libsvm.SVMParameters
import org.tribuo.data.csv.CSVLoader
import org.tribuo.evaluation.TrainTestSplitter

import static com.oracle.labs.mlrg.olcut.provenance.ProvenanceUtil.formattedProvenanceString
import static org.tribuo.classification.libsvm.SVMClassificationType.SVMMode.C_SVC
import static org.tribuo.common.libsvm.KernelType.LINEAR

def url = getClass().classLoader.getResource('iris_data.csv').toURI().toURL()
def data = new CSVLoader<>(new LabelFactory()).loadDataSource(url, 'Class')

def seed = 42L
def splitter = new TrainTestSplitter<>(data, 0.8, seed) // train with 80%
def train = new MutableDataset(splitter.train)
def test = new MutableDataset(splitter.test)
def evaluator = new LabelEvaluator()
println "\nTraining data provenance: ${formattedProvenanceString(train.provenance)}"

def svmParams = [new SVMParameters(new SVMClassificationType(C_SVC), LINEAR)]
def summary = [:]
def trainers = [
        new CARTClassificationTrainer(),
        new LogisticRegressionTrainer(),
        new MultinomialNaiveBayesTrainer(),
        new LibLinearClassificationTrainer(),
        new LibSVMClassificationTrainer(*svmParams),
        new XGBoostClassificationTrainer(4)
]

trainers.each { trainer ->
    def result = evaluator.evaluate(trainer.train(train), test)
    summary[trainer.getClass().simpleName.padRight(35)] = sprintf('%4.2f', result.accuracy())
    println "\n$trainer\n$result"
}

println "\nSummary of results:\n${summary.collect{"$it.key $it.value" }.join('\n')}"
