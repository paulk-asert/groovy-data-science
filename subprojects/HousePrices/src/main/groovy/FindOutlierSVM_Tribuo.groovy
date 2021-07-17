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
import org.tribuo.anomaly.AnomalyFactory
import org.tribuo.anomaly.evaluation.AnomalyEvaluator
import org.tribuo.anomaly.libsvm.LibSVMAnomalyTrainer
import org.tribuo.anomaly.libsvm.SVMAnomalyType
import org.tribuo.common.libsvm.SVMParameters
import org.tribuo.data.columnar.RowProcessor
import org.tribuo.data.columnar.processors.field.DoubleFieldProcessor
import org.tribuo.data.columnar.processors.response.FieldResponseProcessor
import org.tribuo.data.csv.CSVDataSource

import static com.oracle.labs.mlrg.olcut.provenance.ProvenanceUtil.formattedProvenanceString
import static org.tribuo.anomaly.Event.EventType.ANOMALOUS
import static org.tribuo.anomaly.libsvm.SVMAnomalyType.SVMMode.ONE_CLASS
import static org.tribuo.common.libsvm.KernelType.RBF

def uri = getClass().classLoader.getResource('kc_house_data.csv').toURI()
def fieldProcessors = ['bedrooms', 'bathrooms', 'grade'].collectEntries { [(it): new DoubleFieldProcessor(it)] }
def responseProcessor = new FieldResponseProcessor('price', '0', new AnomalyFactory())
def rowProcessor = new RowProcessor(responseProcessor, fieldProcessors)
def dataSource = new CSVDataSource(uri, rowProcessor, true)
def data = new MutableDataset(dataSource)
def eval = new AnomalyEvaluator()

def params = new SVMParameters(new SVMAnomalyType(ONE_CLASS), RBF)
params.gamma = 0.04
params.nu = 0.0008
def trainer = new LibSVMAnomalyTrainer(params)

def model = trainer.train(data)
println "\nFinding anomalies with: ${formattedProvenanceString(model.provenance.trainerProvenance)}"

def predictions = model.predict(data)
def results = predictions.indexed().collect{ i, prediction ->
    [prediction.output, data.getExample(i)]
}
def anomalies = results
        .findAll{it[0].type == ANOMALOUS }
        .sort{ it[0].score }
        .collect{it[1].toList() }
if (anomalies) {
    println "\nPotential anomalies:\n${anomalies.join('\n')}"
}

def testEvaluation = eval.evaluate(model, data)
println "\n$testEvaluation"
println testEvaluation.confusionString()
