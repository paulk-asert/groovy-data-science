import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.SpiderWebPlot
import org.jfree.data.category.DefaultCategoryDataset
import weka.clusterers.SimpleKMeans
import weka.core.Instance
import weka.core.converters.CSVLoader

def file = getClass().classLoader.getResource('whiskey.csv').file as File
//def file = 'whiskey.csv'
def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]

def numClusters = 5
def loader = new CSVLoader(file: file)
def clusterer = new SimpleKMeans(numClusters: numClusters, preserveInstancesOrder: true)
def instances = loader.dataSet
instances.deleteAttributeAt(0) // remove RowID
clusterer.buildClusterer(instances)
println '           ' + cols.join(', ')
def dataset = new DefaultCategoryDataset()
clusterer.clusterCentroids.eachWithIndex{ Instance ctrd, num ->
    print "Cluster ${num+1}: "
    println ((1..cols.size()).collect{ sprintf '%.3f', ctrd.value(it) }.join(', '))
    (1..cols.size()).each { idx ->
        dataset.addValue(ctrd.value(idx), "Cluster ${num+1}", cols[idx-1]) }
}

def clusters = (0..<numClusters).collectEntries{ [it, []] }
clusterer.assignments.eachWithIndex { cnum, idx ->
    clusters[cnum] << instances.get(idx).stringValue(0) }
clusters.each { k, v ->
    println "Cluster ${k+1}:"
    println v.join(', ')
}

def plot = new SpiderWebPlot(dataset: dataset)
def chart = new JFreeChart('Whiskey clusters', plot)
SwingUtil.show(new ChartPanel(chart))
