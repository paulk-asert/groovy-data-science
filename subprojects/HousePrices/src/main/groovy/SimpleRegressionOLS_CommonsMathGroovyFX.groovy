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
import org.apache.commons.math3.stat.regression.SimpleRegression
import static groovyx.javafx.GroovyFX.start
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

def feature = 'bedrooms'
def nonOutliers = feature == 'bedrooms' ? { it[0] < 30 } : { true }
def file = getClass().classLoader.getResource('kc_house_data.csv').file
def csv  = CSV.withFirstRecordAsHeader().parse(new FileReader(file))
def all  = csv.collect { [it[feature].toDouble(), it.price.toDouble()] }.findAll(nonOutliers)
def reg = new SimpleRegression().tap{ addData(all as double[][]) }
def (min, max) = all.transpose().with{ [it[0].min(), it[0].max()] }
def predicted = [[min, reg.predict(min)], [max, reg.predict(max)]]

start {
    stage(title: "Price vs $feature", show: true, width: 800, height: 600) {
        scene {
//            NOTE using css trick to allow multiple chart types
//            TODO consider using JavaFXMultiChart
//            scatterChart(opacity: 50) {
//                series(name: 'Actual', data: all)
//            }
//            lineChart {
            lineChart(stylesheets: resource('/style.css')) {
                series(name: 'Actual', data: all)
                series(name: 'Predicted', data: predicted)
            }
        }
    }
}
/* */
