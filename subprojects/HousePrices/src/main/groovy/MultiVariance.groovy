import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder

import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV
import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Line
import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Scatter
import static org.knowm.xchart.style.markers.SeriesMarkers.NONE

def file = getClass().classLoader.getResource('kc_house_data.csv').file
def csv  = CSV.withFirstRecordAsHeader().parse(new FileReader(file))
def all  = csv.toList()
def price = all.collect{ it[2].toDouble() }
def features = all.collect{ it.toList()[3..-1]*.toDouble() }
def reg = new OLSMultipleLinearRegression()
reg.newSampleData(price as double[], features as double[][])
def betas = reg.estimateRegressionParameters()
def predicted = features.collect{ row -> row.indices.collect{ i -> betas[i+1] * row[i] }.sum() + betas[0] }

def chart = new XYChartBuilder().width(900).height(450).title("Actual vs predicted price").xAxisTitle("Actual").yAxisTitle("Predicted").build()
chart.addSeries("Price", price as double[], predicted as double[]).with {
    XYSeriesRenderStyle = Scatter
}
def from = [price.min(), predicted.min()].min()
def to = [price.max(), predicted.max()].min()
chart.addSeries("exact", [from, to] as double[], [from, to] as double[]).with {
    marker = NONE
    XYSeriesRenderStyle = Line
}
new SwingWrapper(chart).displayChart()
