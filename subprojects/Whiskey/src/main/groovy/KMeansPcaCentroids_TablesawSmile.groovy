//@Grab('tech.tablesaw:tablesaw-core:0.34.1')
//@Grab('tech.tablesaw:tablesaw-aggregate:0.34.1')
import smile.clustering.KMeans
import smile.projection.PCA
import tech.tablesaw.api.*
import tech.tablesaw.plotly.Plot
import tech.tablesaw.plotly.api.*

def file = getClass().classLoader.getResource('whiskey.csv').file
def rows = Table.read().csv(file)
//Table rows = Table.read().csv('whiskey.csv')

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]
def data = rows.as().doubleMatrix(*cols)

def pca = new PCA(data)
def dims = 3
pca.projection = dims
def projected = pca.project(data)
def clusterer = new KMeans(data, 5)
def labels = clusterer.clusterLabel.collect { "Cluster " + (it + 1) }
rows = rows.addColumns(
    *(0..<dims).collect { idx ->
        DoubleColumn.create("PCA${idx+1}", (0..<data.size()).collect{
            projected[it][idx]
        })
    },
    StringColumn.create("Cluster", labels),
    DoubleColumn.create("Centroid", [10] * labels.size())
)
def centroids = pca.project(clusterer.centroids())
def toAdd = rows.emptyCopy(1)
(0..<centroids.size()).each { idx ->
    toAdd[0].setString("Cluster", "Cluster " + (idx+1))
    (1..3).each { toAdd[0].setDouble("PCA" + it, centroids[idx][it-1]) }
    toAdd[0].setDouble("Centroid", 50)
    rows.append(toAdd)
}

def title = "Clusters x Principal Components w/ centroids"
def type = dims == 2 ? ScatterPlot : Scatter3DPlot
Plot.show(type.create(title, rows, *(1..dims).collect { "PCA$it" }, "Centroid", "Cluster"))
