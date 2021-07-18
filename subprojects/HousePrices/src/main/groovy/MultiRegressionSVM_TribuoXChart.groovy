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

import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.tribuo.MutableDataset
import org.tribuo.common.libsvm.SVMParameters
import org.tribuo.data.columnar.RowProcessor
import org.tribuo.data.columnar.processors.field.DoubleFieldProcessor
import org.tribuo.data.columnar.processors.response.FieldResponseProcessor
import org.tribuo.data.csv.CSVDataSource
import org.tribuo.regression.RegressionFactory
import org.tribuo.regression.libsvm.LibSVMRegressionTrainer
import org.tribuo.regression.libsvm.SVMRegressionType

import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Line
import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Scatter
import static org.knowm.xchart.style.markers.SeriesMarkers.NONE
import static org.tribuo.regression.libsvm.SVMRegressionType.SVMMode.EPSILON_SVR
import static org.tribuo.common.libsvm.KernelType.LINEAR

def cols = ['bedrooms','bathrooms','sqft_living','sqft_lot','floors','waterfront','view','condition','grade',
            'sqft_above','sqft_basement','yr_built','yr_renovated','zipcode','lat','long','sqft_living15','sqft_lot15']
def fieldProcessors = cols.collectEntries{ [it, new DoubleFieldProcessor(it)] }
def responseProcessor = new FieldResponseProcessor('price', '0', new RegressionFactory())
def rowProcessor = new RowProcessor(responseProcessor, fieldProcessors)

def uri = getClass().classLoader.getResource('kc_house_data.csv').toURI()
def dataSource = new CSVDataSource(uri, rowProcessor, true)
def data = new MutableDataset(dataSource)
def actuals = data.collect{ it.output.values[0] }

def svmParams = new SVMParameters(new SVMRegressionType(EPSILON_SVR), LINEAR)

def trainer = new LibSVMRegressionTrainer(svmParams)
println 'Training (this may take a while) ...'
def model = trainer.train(data)
def predictions = model.predict(data).collect{it.output.values[0] }

def chart = new XYChartBuilder().width(900).height(450).title("Actual vs predicted price").xAxisTitle("Actual").yAxisTitle("Predicted").build()
chart.addSeries("Price", actuals as double[], predictions as double[]).with {
    XYSeriesRenderStyle = Scatter
}
def min = [actuals.min(), predictions.min()].min()
def max = [actuals.max(), predictions.max()].max()
def range = [min, max] as double[]
chart.addSeries("Ideal", range, range).with {
    marker = NONE
    XYSeriesRenderStyle = Line
}
new SwingWrapper(chart).displayChart()
