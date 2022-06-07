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

import groovy.transform.CompileStatic
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.tribuo.Dataset
import org.tribuo.Example
import org.tribuo.MutableDataset
import org.tribuo.data.columnar.FieldProcessor
import org.tribuo.data.columnar.RowProcessor
import org.tribuo.data.columnar.processors.field.DoubleFieldProcessor
import org.tribuo.data.columnar.processors.response.FieldResponseProcessor
import org.tribuo.data.csv.CSVDataSource
import org.tribuo.math.optimisers.AdaGrad
import org.tribuo.regression.RegressionFactory
import org.tribuo.regression.Regressor
import org.tribuo.regression.sgd.linear.LinearSGDTrainer
import org.tribuo.regression.sgd.objectives.SquaredLoss

import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Line
import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Scatter
import static org.knowm.xchart.style.markers.SeriesMarkers.NONE

// This example uses Groovy 3 'var' which is fine for JDK8+,
// but if you need to run on Groovy 2.x, change back to 'def'.

// Groovy doesn't need static compilation for this library but we can use it if we want as shown below
@CompileStatic
List regress(URI uri) {
    var cols = ['bedrooms', 'bathrooms', 'sqft_living', 'sqft_lot', 'floors', 'waterfront', 'view', 'condition', 'grade',
                'sqft_above', 'sqft_basement', 'yr_built', 'yr_renovated', 'zipcode', 'lat', 'long', 'sqft_living15', 'sqft_lot15']
    Map<String, FieldProcessor> fieldProcessors = cols.collectEntries { [it, new DoubleFieldProcessor(it)] }
    var responseProcessor = new FieldResponseProcessor('price', '0', new RegressionFactory())
    var rowProcessor = new RowProcessor(responseProcessor, fieldProcessors)
    var dataSource = new CSVDataSource(uri, rowProcessor, true)
    Dataset<Regressor> data = new MutableDataset<>(dataSource)
    var actuals = data.stream().map((Example<Regressor> e) -> e.output.values[0]).toList()

    var trainer = new LinearSGDTrainer(new SquaredLoss(), new AdaGrad(50.0d), 10, 1L)
    var model = trainer.train(data)
    println '\nTop features:\n' + model.getTopFeatures(10).values()[0].collect{ "${it.a - '@value'} $it.b"}
    println "Weights:\n$model.weightsCopy"
    var predictions = model.predict(data).collect { it.output.values[0] }
    [actuals as double[], predictions as double[]]
}

var uri = getClass().classLoader.getResource('kc_house_data.csv').toURI()
def (actuals, predictions) = regress(uri)

var chart = new XYChartBuilder().width(900).height(450).title("Actual vs predicted price").xAxisTitle("Actual").yAxisTitle("Predicted").build()
chart.addSeries("Price", actuals, predictions).with {
    XYSeriesRenderStyle = Scatter
}
var min = [actuals.min(), predictions.min()].min()
var max = [actuals.max(), predictions.max()].max()
var range = [min, max] as double[]
chart.addSeries("Ideal", range, range).with {
    marker = NONE
    XYSeriesRenderStyle = Line
}
new SwingWrapper(chart).displayChart()
