import smile.clustering.HierarchicalClustering
import smile.clustering.linkage.WardLinkage
import smile.math.Math
import smile.plot.Dendrogram
import tech.tablesaw.api.Table

def file = getClass().classLoader.getResource('whiskey.csv').file
def rows = Table.read().csv(file)
//Table rows = Table.read().csv('whiskey.csv')

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]
def data = rows.as().doubleMatrix(*cols)

int n = data.length
double[][] proximity = new double[n][]
for (i in 0..<n) {
    proximity[i] = new double[i + 1]
    for (j in 0..<i) proximity[i][j] = Math.distance(data[i], data[j])
}
def clusterer = new HierarchicalClustering(new WardLinkage(proximity))
//println clusterer.tree
//println clusterer.height
def plot = Dendrogram.plot(clusterer.tree, clusterer.height)
plot.title = "Dendrogram"

SwingUtil.show(plot)
