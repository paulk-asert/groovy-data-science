import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import weka.classifiers.functions.LinearRegression
import weka.core.converters.CSVLoader
import weka.filters.Filter
import weka.filters.unsupervised.attribute.Remove

import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Line
import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Scatter
import static org.knowm.xchart.style.markers.SeriesMarkers.NONE

def file = getClass().classLoader.getResource('kc_house_data.csv').file as File

def loader = new CSVLoader(file: file)
def model = new LinearRegression()
def allInstances = loader.dataSet
def priceIndex = 2
allInstances.setClassIndex(priceIndex)
// remove "id" and "date" columns
def rm = new Remove(attributeIndices: '1,2', inputFormat: allInstances)
def instances = Filter.useFilter(allInstances, rm)
model.buildClassifier(instances)
println model.coefficients()

def actual = instances.collect{ it.value(0).toDouble() }
def predicted = instances.collect{ model.classifyInstance(it) }

def chart = new XYChartBuilder().width(900).height(450).title("Actual vs predicted price").xAxisTitle("Actual").yAxisTitle("Predicted").build()
chart.addSeries("Price", actual as double[], predicted as double[]).with {
    XYSeriesRenderStyle = Scatter
}
def from = [actual.min(), predicted.min()].min()
def to = [actual.max(), predicted.max()].min()
chart.addSeries("exact", [from, to] as double[], [from, to] as double[]).with {
    marker = NONE
    XYSeriesRenderStyle = Line
}
new SwingWrapper(chart).displayChart()
