import com.datumbox.framework.core.common.dataobjects.Dataframe
import com.datumbox.framework.common.Configuration
import com.datumbox.framework.core.machinelearning.MLBuilder
import com.datumbox.framework.core.machinelearning.clustering.Kmeans
import com.datumbox.framework.core.machinelearning.featureselection.PCA
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.SpiderWebPlot
import org.jfree.chart.plot.XYPlot
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.DefaultXYZDataset

import static com.datumbox.framework.common.dataobjects.TypeInference.DataType.NUMERICAL
import static com.datumbox.framework.common.dataobjects.TypeInference.DataType.CATEGORICAL
import static com.datumbox.framework.core.machinelearning.clustering.Kmeans.TrainingParameters.Distance.EUCLIDIAN
import static com.datumbox.framework.core.machinelearning.clustering.Kmeans.TrainingParameters.Initialization.FORGY

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]
def k = 4

def config = Configuration.configuration
def headers = [*:cols.collectEntries{[it, NUMERICAL] }, Distillery: CATEGORICAL]
Dataframe df = null
new File(getClass().classLoader.getResource('whiskey.csv').file).withReader {
    df = Dataframe.Builder.parseCSVFile(it, 'Distillery', headers, ',' as char, '"' as char, "\r\n", null, null, config)
}

def trainParams = new Kmeans.TrainingParameters(k: k, maxIterations: 100, initializationMethod: FORGY,
        distanceMethod: EUCLIDIAN, weighted: false)

Kmeans clusterer = MLBuilder.create(trainParams, config)
clusterer.fit(df)
clusterer.predict(df)
def cl = clusterer.clusters
def centroids = new DefaultCategoryDataset()
println 'Centroids ' + cols.join(', ')
cl.each { idx, v ->
    def ctrd = v.centroid.x
    println "Cluster$idx: " + cols.collect{sprintf '%.3f', ctrd.get(it) }.join(', ')
    cols.each { col ->
        centroids.addValue(ctrd.get(col), "Cluster $idx", col)
    }
}

def centroidPlot = new SpiderWebPlot(dataset: centroids)
def centroidChart = new JFreeChart('Centroid spider plot', centroidPlot)

def pcaParams = new PCA.TrainingParameters(whitened: true, maxDimensions: k)
PCA featureSelector = MLBuilder.create(pcaParams, config)
featureSelector.fit_transform(df)
featureSelector.close()

def xyz = new DefaultXYZDataset()
def clusters = [:].withDefault{[]}
def (xs, ys, zs) = [[:].withDefault{[]}, [:].withDefault{[]}, [:].withDefault{[]}]
def zmin = df.entries().collect{it.value.x.get(2) }.min()
def zmax = df.entries().collect{it.value.x.get(2) }.max()

df.entries().each{ e ->
    def idx = e.value.YPredicted
    clusters["Cluster$idx"] << e.value.y
    xs[idx] << -e.value.x.get(0)
    ys[idx] << e.value.x.get(1)
    zs[idx] << 0.5 + (e.value.x.get(2) - zmin)/(zmax - zmin) // normalize
}
df.delete()

(0..<k).each{
    xyz.addSeries("Cluster $it:", [xs[it], ys[it], zs[it]] as double[][])
}

println clusters
        .collectEntries{ e -> [e.key, e.value.join(', ')] }
        .sort{e -> e.key }
        .collect{ idx, v -> "$idx: $v" }.join('\n')

def xaxis = new NumberAxis(label: "PCA1", autoRange: false, lowerBound: -6, upperBound: 10)
def yaxis = new NumberAxis(label: "PCA2", autoRange: false, lowerBound: -9, upperBound: 0)
def bubblePlot = new XYPlot(xyz, xaxis, yaxis, JFreeChartUtil.bubbleRenderer())
def bubbleChart = new JFreeChart('PCA bubble plot', bubblePlot)

SwingUtil.show(new ChartPanel(centroidChart), new ChartPanel(bubbleChart),
        size: [400, 800],
        title: 'Whiskey clusters: CSV,kmeans,PCA=datumbox plot=jfreechart'
)
