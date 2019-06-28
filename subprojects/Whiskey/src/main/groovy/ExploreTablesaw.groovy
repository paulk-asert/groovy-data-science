//@Grab('tech.tablesaw:tablesaw-core:0.34.1')
//@Grab('tech.tablesaw:tablesaw-aggregate:0.34.1')
import smile.clustering.KMeans
import smile.projection.PCA
import tech.tablesaw.api.*
import tech.tablesaw.plotly.Plot
import tech.tablesaw.plotly.api.*

// source: https://www.niss.org/sites/default/files/ScotchWhisky01.txt
def file = getClass().classLoader.getResource('whiskey.csv').file
def rows = Table.read().csv(file)
//Table rows = Table.read().csv('whiskey.csv')

def cols = ["Body", "Sweetness", "Smoky",
            "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty",
            "Malty", "Fruity", "Floral"]
def data = rows.as().doubleMatrix(*cols)

def pca = new PCA(data)
pca.projection = 2
def projected = pca.project(data)

def kMeans = new KMeans(data, 5)
rows = rows.addColumns(
    DoubleColumn.create("PCA1", (0..<data.size()).collect{ projected[it][0] }),
    DoubleColumn.create("PCA2", (0..<data.size()).collect{ projected[it][1] }),
    StringColumn.create("Cluster", kMeans.clusterLabel.collect{ it.toString() })
)

def title = "Clusters x Principal Components"
Plot.show(ScatterPlot.create(title, rows, "PCA1", "PCA2", "Cluster"))
