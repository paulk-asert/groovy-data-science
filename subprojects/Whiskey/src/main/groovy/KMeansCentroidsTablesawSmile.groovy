//@Grab('tech.tablesaw:tablesaw-core:0.34.1')
//@Grab('tech.tablesaw:tablesaw-aggregate:0.34.1')
import smile.clustering.KMeans
import smile.projection.PCA
import tech.tablesaw.api.*
import tech.tablesaw.plotly.Plot
import tech.tablesaw.plotly.api.*

def file = getClass().classLoader.getResource('whiskey.csv').file
def table = Table.read().csv(file)
//def table = Table.read().csv('whiskey.csv')

def cols = ["Body", "Sweetness", "Smoky", "Medicinal",
            "Tobacco", "Honey", "Spicy", "Winey",
            "Nutty", "Malty", "Fruity", "Floral"]
def data = table.as().doubleMatrix(*cols)

def pca = new PCA(data)
pca.projection = 3
def projected = pca.project(data)
def groups = new KMeans(data, 5)
table = table.addColumns(
    *(0..<3).collect { idx ->
        DoubleColumn.create("PCA${idx+1}", (0..<data.size()).collect{
            projected[it][idx]
        })
    },
    StringColumn.create("Cluster", groups.clusterLabel.collect{ "Cluster " + (it+1) }),
    DoubleColumn.create("Centroid", groups.clusterLabel.collect{ 10 })
)
def centroids = pca.project(groups.centroids())
def toAdd = table.emptyCopy(1)
(0..<centroids.size()).each { idx ->
    toAdd[0].setString("Cluster", "Cluster " + (idx+1))
    (1..3).each { toAdd[0].setDouble("PCA" + it, centroids[idx][it-1]) }
    toAdd[0].setDouble("Centroid", 50)
    table.append(toAdd)
}

def title = "Clusters x Principal Components w/ centroids"
Plot.show(Scatter3DPlot.create(title, table, *(1..3).collect { "PCA$it" }, "Centroid", "Cluster"))
