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
import org.tribuo.classification.evaluation.LabelEvaluator
import org.tribuo.classification.sgd.linear.LinearSGDTrainer
import org.tribuo.classification.sgd.objectives.LogMulticlass
import org.tribuo.datasource.IDXDataSource
import org.tribuo.math.optimisers.AdaGrad

import java.nio.file.Paths

def labelFactory = new LabelFactory()
def trainSource = new IDXDataSource(Paths.get("/path/to/train-images-idx3-ubyte.gz"),
        Paths.get("/path/to/train-labels-idx1-ubyte.gz"), labelFactory)
def train = new MutableDataset(trainSource)
def trainer = new LinearSGDTrainer(new LogMulticlass(), new AdaGrad(0.5), 5, 42)

println '\nTraining ...'
def model = trainer.train(train)

def testSource = new IDXDataSource<>(Paths.get("/path/to/t10k-images-idx3-ubyte.gz"),
        Paths.get("/path/to/t10k-labels-idx1-ubyte.gz"), labelFactory)
println '\nTesting ...'
def predictions = model.predict(testSource)

def evaluator = new LabelEvaluator()
def evaluation = evaluator.evaluate(model, predictions, testSource.provenance)
println "\nEvaluation:\n$evaluation"
