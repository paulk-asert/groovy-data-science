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

def cols = ["Body", "Sweetness", "Smoky", "Medicinal",
            "Tobacco", "Honey", "Spicy", "Winey",
            "Nutty", "Malty", "Fruity", "Floral"]
def data = rows.as().doubleMatrix(*cols)

def pca = new PCA(data)
def dims = 4 // can be 2, 3 or 4
pca.projection = dims
def projected = pca.project(data)
def adj = [1, 1, 1, 5]
def groups = new KMeans(data, 5)
rows = rows.addColumns(
    *(0..<dims).collect { idx ->
        DoubleColumn.create("PCA${idx+1}", (0..<data.size()).collect{
            adj[idx] * (projected[it][idx] + adj[idx])
        })
    },
    StringColumn.create("Cluster", groups.clusterLabel.collect{ "Cluster" + (it+1) })
)

def title = "Clusters x Principal Components"
def type = dims == 2 ? ScatterPlot : Scatter3DPlot
Plot.show(type.create(title, rows, *(1..dims).collect { "PCA$it" }, "Cluster"))
