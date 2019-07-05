import groovy.swing.SwingBuilder
import smile.clustering.HierarchicalClustering
import smile.clustering.linkage.WardLinkage
import smile.math.Math
import smile.plot.Dendrogram
import tech.tablesaw.api.Table

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE as DISPOSE

// source: https://www.niss.org/sites/default/files/ScotchWhisky01.txt
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
def hac = new HierarchicalClustering(new WardLinkage(proximity))
//println hac.tree
//println hac.height
plot = Dendrogram.plot(hac.tree, hac.height)
plot.title = "Dendrogram"

new SwingBuilder().edt {
    frame(title: 'Frame', size: [800, 600], show: true, defaultCloseOperation: DISPOSE) {
        widget(plot)
    }
}
