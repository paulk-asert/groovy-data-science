import smile.clustering.KMeans
import smile.projection.PCA
import tech.tablesaw.api.*
import tech.tablesaw.plotly.Plot
import tech.tablesaw.plotly.api.*

def file = getClass().classLoader.getResource('whiskey.csv').file
def table = Table.read().csv(file)
//def table = Table.read().csv('whiskey.csv')

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]
def data = table.as().doubleMatrix(*cols)

def pca = new PCA(data)
pca.projection = 3
def projected = pca.project(data)
def clusterer = new KMeans(data, 5)
def labels = clusterer.clusterLabel.collect { "Cluster " + (it + 1) }
table = table.addColumns(
    *(0..<3).collect { idx ->
        DoubleColumn.create("PCA${idx+1}", (0..<data.size()).collect{
            projected[it][idx]
        })},
    StringColumn.create("Cluster", labels),
    DoubleColumn.create("Centroid", [10] * labels.size())
)
def centroids = pca.project(clusterer.centroids())
def toAdd = table.emptyCopy(1)
(0..<centroids.size()).each { idx ->
    toAdd[0].setString("Cluster", "Cluster " + (idx+1))
    (1..3).each { toAdd[0].setDouble("PCA" + it, centroids[idx][it-1]) }
    toAdd[0].setDouble("Centroid", 50)
    table.append(toAdd)
}

def title = "Clusters x Principal Components w/ centroids"
Plot.show(Scatter3DPlot.create(title, table, *(1..3).collect { "PCA$it" }, "Centroid", "Cluster"))
