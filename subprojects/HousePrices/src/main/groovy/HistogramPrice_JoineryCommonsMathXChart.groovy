// joinery has some neat capabilities but has a restrictive GPL3 license
import joinery.DataFrame
import org.apache.commons.math3.random.EmpiricalDistribution
import org.knowm.xchart.CategoryChartBuilder
import org.knowm.xchart.Histogram
import org.knowm.xchart.SwingWrapper

def binCount = 50
def is = getClass().classLoader.getResourceAsStream('kc_house_data.csv')
def price = DataFrame.readCsv(is).select{ values -> values[3] < 30 }.retain("price")
def dist = new EmpiricalDistribution(binCount).tap{ load(price.toArray(double[])) }
def hist1 = new DataFrame("idx", "price")
dist.binStats.withIndex().each { v, i -> hist1.append([i, v.n]) }
hist1 = hist1.retain("price")
hist1.plot(DataFrame.PlotType.BAR)

// hist.plot use an older version of xchart under the covers
// we can also use xchart directly (using new version shown)

def hist2 = new Histogram(price.collect{ it[0] }, binCount)
def chart = new CategoryChartBuilder().width(900).height(450)
        .title("Price Histogram").xAxisTitle("Price").yAxisTitle("Count").build()
chart.addSeries("Price", hist2.xAxisData, hist2.yAxisData)
chart.styler.with {
    XAxisLabelRotation = 90
    availableSpaceFill = 0.98
    XAxisMin = 0
    XAxisMin = 8_000_000
}
new SwingWrapper(chart).displayChart()
