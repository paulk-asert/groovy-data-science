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
import smile.data.DataFrame
import smile.data.formula.Formula
import smile.io.Read
import smile.plot.swing.LinePlot
import smile.plot.swing.ScatterPlot
import smile.regression.OLS

import static java.awt.Color.BLUE
import static java.awt.Color.RED
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV
import static smile.plot.swing.Line.Style.DASH

var file = getClass().classLoader.getResource('kc_house_data.csv').file as File
var table = Read.csv(file.toPath(), CSV.withFirstRecordAsHeader())
table = table.drop(0, 1) // remove 'id' and 'date'
var filtered = table.toList().findAll { it.apply('bedrooms') <= 30 }
table = DataFrame.of(filtered)

var price = table.column('price').toDoubleArray()
var reg = OLS.fit(Formula.lhs('price'), table)
var predictors = table.drop([0] as int[]) // remove 'price'
var predicted = predictors.toArray().collect{ reg.predict(it) } as double[]
double[][] data = [price, predicted].transpose()

var from = [price.toList().min(), predicted.min()].min()
var to = [price.toList().max(), predicted.max()].max()
var ideal = LinePlot.of([[from, from], [to, to]] as double[][], DASH, RED)

ScatterPlot.of(data, BLUE).canvas().with {
    title = 'Actual vs predicted price'
    setAxisLabels('Actual', 'Predicted')
    add(ideal)
    window()
}
