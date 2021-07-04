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

import com.edwardraff.jsatfx.Plot
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import jsat.DataSet
import jsat.classifiers.bayesian.NaiveBayes
import jsat.datatransform.PCA
import jsat.datatransform.ZeroMeanTransform
import jsat.io.CSV

import javax.swing.JFrame

// JSAT has some interesting algorithms but has a restrictive license:
// https://github.com/EdwardRaff/JSAT/wiki/Algorithms

def file = getClass().classLoader.getResource('iris_data.csv').file as File

def species = ['Iris-setosa', 'Iris-versicolor', 'Iris-virginica']

DataSet data = CSV.readC(4, file.toPath(), 1, [] as Set)

int errors = 0
def classifier = new NaiveBayes()
classifier.trainC(data)

boolean first = true
for (i in 0..<data.sampleSize) {
    def sample = data.getDataPoint(i)
    int truth = data.getDataPointCategory(i)

    def results = classifier.classify(sample)
    int predicted = results.mostLikely()
    if (predicted != truth) {
        errors++
        if (first) {
            first = false
            println "Sample            Actual         Predicted  Confidence: setosa     versicolor virginica"
        }
        println "$i".padLeft(6) + "${species[truth]}".padLeft(18) +
                "${species[predicted]}".padLeft(18) +
                "              ${(0..2).collect{ sprintf '%4.2f       ', results.getProb(it) }.join()}"
    }
}

println "$errors errors were made, ${100.0 * errors / data.sampleSize}% error rate"

// normalize data samples
data.applyTransform(new ZeroMeanTransform(data))
data.applyTransform(new PCA(data, 2, 1e-5))

def jFrame = new JFrame(title: "PCA Visualization", defaultCloseOperation: JFrame.EXIT_ON_CLOSE)
def panel = new JFXPanel()
jFrame.add(panel)
jFrame.setSize(600, 600)
jFrame.visible = true

Platform.runLater(() -> { panel.scene = new Scene(Plot.scatterC(data)) })
