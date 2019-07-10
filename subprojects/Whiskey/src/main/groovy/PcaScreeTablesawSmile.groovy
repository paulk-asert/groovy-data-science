import groovy.swing.SwingBuilder
import java.awt.Color
import smile.clustering.KMeans
import smile.plot.PlotCanvas
import smile.plot.PlotPanel
import smile.plot.ScatterPlot
import smile.projection.PCA
import tech.tablesaw.api.DoubleColumn
import tech.tablesaw.api.IntColumn
import tech.tablesaw.api.Table

import static java.awt.Color.*
import static javax.swing.JFrame.DISPOSE_ON_CLOSE as DISPOSE

def file = getClass().classLoader.getResource('whiskey.csv').file
def table = Table.read().csv(file)
//def table = Table.read().csv('whiskey.csv')
table = table.removeColumns(0)

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]
def data = table.as().doubleMatrix(*cols)

def pca = new PCA(data)
pca.projection = 2
def plots = [PlotCanvas.screeplot(pca)]
def projected = pca.project(data)
table = table.addColumns(
        *(1..2).collect { idx ->
            DoubleColumn.create("PCA$idx", (0..<data.size()).collect { projected[it][idx - 1] })
        }
)

def colors = [RED, BLUE, GREEN, ORANGE, MAGENTA, GRAY]
def symbols = ['*', 'Q', '#', 'Q', '*', '#']
(2..6).each { k ->
    def clusterer = new KMeans(data, k)
    double[][] components = table.as().doubleMatrix('PCA1', 'PCA2')
    plots << ScatterPlot.plot(components, clusterer.clusterLabel, symbols[0..<k] as char[], colors[0..<k] as Color[])
}

new SwingBuilder().edt {
    frame(title: 'Frame', size: [1200, 900], show: true, defaultCloseOperation: DISPOSE) {
        widget(new PlotPanel(*plots))
    }
}
