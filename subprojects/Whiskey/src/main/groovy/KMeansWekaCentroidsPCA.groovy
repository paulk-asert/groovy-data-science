import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.labels.StandardXYToolTipGenerator
import org.jfree.chart.plot.SpiderWebPlot
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYBubbleRenderer
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.DefaultXYZDataset
import weka.attributeSelection.PrincipalComponents
import weka.clusterers.SimpleKMeans
import weka.core.Instance
import weka.core.converters.CSVLoader

import java.awt.Color

def file = getClass().classLoader.getResource('whiskey.csv').file as File
//def file = 'src/main/resources/whiskey.csv'
def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]

def numClusters = 4
def loader = new CSVLoader(file: file)
def clusterer = new SimpleKMeans(numClusters: numClusters, preserveInstancesOrder: true)
def instances = loader.dataSet
instances.deleteAttributeAt(0) // remove RowID
clusterer.buildClusterer(instances)
println '           ' + cols.join(', ')
def category = new DefaultCategoryDataset()
def xyz = new DefaultXYZDataset()

clusterer.clusterCentroids.eachWithIndex{ Instance ctrd, num ->
    print "Cluster ${num+1}: "
    println ((1..cols.size()).collect{ sprintf '%.3f', ctrd.value(it) }.join(', '))
    (1..cols.size()).each { idx ->
        category.addValue(ctrd.value(idx), "Cluster ${num+1}", cols[idx-1]) }
}

PrincipalComponents pca = new PrincipalComponents()
pca.buildEvaluator(instances)
pca.setVarianceCovered(0.9)
pca.setMaximumAttributeNames(3)
//println pca
def transformed = pca.transformedData(instances)
def clusters = (0..<numClusters).collectEntries{ [it, []] }
def (x, y, z) = [[:].withDefault{[]}, [:].withDefault{[]}, [:].withDefault{[]}]
def zvalues = (0..<transformed.numInstances()).collect{transformed.get(it).value(2) }
def (zmin, zmax) = [zvalues.min(), zvalues.max()]

clusterer.assignments.eachWithIndex { cnum, idx ->
    clusters[cnum] << instances.get(idx).stringValue(0)
    x[cnum] << transformed.get(idx).value(0)
    y[cnum] << transformed.get(idx).value(1)
    z[cnum] << (transformed.get(idx).value(2) - zmin + 0.5)/(zmax - zmin) * 2
}

clusters.each { k, v ->
    println "Cluster ${k+1}:"
    println v.join(', ')
    xyz.addSeries("Cluster ${k+1}:", [x[k], y[k], z[k]] as double[][])
}

def spiderPlot = new SpiderWebPlot(dataset: category)
def spiderChart = new JFreeChart('Centroids spider plot', spiderPlot)
def spiderPanel = new ChartPanel(spiderChart)

def r = new XYBubbleRenderer()
r.setDefaultToolTipGenerator(new StandardXYToolTipGenerator())
// default colors are solid, make some semi-transparent ones
r.setSeriesPaint(0, new Color(1, 0, 0, 0.2f))
r.setSeriesPaint(1, new Color(0, 0, 1, 0.2f))
r.setSeriesPaint(2, new Color(0, 1, 0, 0.2f))
r.setSeriesPaint(3, new Color(1, 1, 0, 0.2f))
def xaxis = new NumberAxis(label: "x", autoRange: false, lowerBound: -5, upperBound: 10)
def yaxis = new NumberAxis(label: "y", autoRange: false, lowerBound: -7, upperBound: 5)
def bubblePlot = new XYPlot(xyz, xaxis, yaxis, r)
def bubbleChart = new JFreeChart('PCA bubble plot', bubblePlot)
def bubblePanel = new ChartPanel(bubbleChart)

SwingUtil.showH(spiderPanel, bubblePanel,
        title: 'Whiskey clusters: Weka=CSV,KMeans,PCA JFreeChart=plots',
        size: [800, 400]
)
