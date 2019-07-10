import groovy.swing.SwingBuilder
import smile.plot.*
import smile.vq.SOM
import tech.tablesaw.api.Table

import static javax.swing.JFrame.DISPOSE_ON_CLOSE as DISPOSE

def file = getClass().classLoader.getResource('whiskey.csv').file
def table = Table.read().csv(file)
//def table = Table.read().csv('whiskey.csv')
//table = table.removeColumns(0)

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]
def data = table.as().doubleMatrix(*cols)
def som = new SOM(data, 100)
def k = 3
def clusters = som.partition(k)
def byName = { table.row(it.intValue()).getText('Distillery') }
(0..<k).each { idx ->
    println "Cluster $idx: " + clusters.findIndexValues { it == idx }.collect(byName).join(', ')
}

def canvas = Hexmap.plot(som.umatrix(), Palette.heat(256))

new SwingBuilder().edt {
    frame(title: 'Frame', size: [800, 600], show: true, defaultCloseOperation: DISPOSE) {
        widget(canvas)
    }
}
