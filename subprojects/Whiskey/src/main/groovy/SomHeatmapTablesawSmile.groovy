import groovy.swing.SwingBuilder
import java.awt.Color
import smile.plot.*
import smile.projection.PCA
import smile.vq.SOM
import tech.tablesaw.api.*

import static java.awt.Color.*
import static javax.swing.JFrame.DISPOSE_ON_CLOSE as DISPOSE

def file = getClass().classLoader.getResource('whiskey.csv').file
def table = Table.read().csv(file)
//def table = Table.read().csv('whiskey.csv')

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]
def data = table.as().doubleMatrix(*cols)
def som = new SOM(data, 40)
def k = 3
def clusters = som.partition(k)
def byName = { table.row(it.intValue()).getText('Distillery') }
(0..<k).each { idx ->
    println "Cluster $idx: " + clusters.findIndexValues { it == idx }.collect(byName).join(', ')
}

def hexmap = Hexmap.plot(som.umatrix(), Palette.heat(256))
/* */
new SwingBuilder().edt {
    frame(title: 'Frame', size: [800, 600], show: true, defaultCloseOperation: DISPOSE) {
        widget(new PlotPanel(scatter, hexmap))
    }
}
/* */
/*
def pca = new PCA(data)
pca.projection = 2
def projected = pca.project(data)
table = table.addColumns(
        *(1..2).collect { idx ->
            DoubleColumn.create("PCA$idx", (0..<data.size()).collect { projected[it][idx - 1] })
        }
)

Color[] colors = [RED, BLUE, GREEN]
char[] symbols = ['*', 'Q', '#']
double[][] components = table.as().doubleMatrix('PCA1', 'PCA2')
def scatter = ScatterPlot.plot(components, clusters, symbols, colors)

new SwingBuilder().edt {
    frame(title: 'Frame', size: [800, 600], show: true, defaultCloseOperation: DISPOSE) {
        widget(new PlotPanel(scatter, hexmap))
    }
}
/* */
