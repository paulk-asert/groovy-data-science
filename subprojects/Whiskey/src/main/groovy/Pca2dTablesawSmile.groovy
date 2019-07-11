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
pca.projection = 2
def projected = pca.project(data)
def clusterer = new KMeans(data, 5)

table = table.addColumns(
        DoubleColumn.create("PCA1", (0..<data.size()).collect{ projected[it][0] }),
        DoubleColumn.create("PCA2", (0..<data.size()).collect{ projected[it][1] }),
        StringColumn.create("Cluster", clusterer.clusterLabel.collect{ it.toString() })
)

def title = "Clusters x Principal Components"
Plot.show(ScatterPlot.create(title, table, "PCA1", "PCA2", "Cluster"))
