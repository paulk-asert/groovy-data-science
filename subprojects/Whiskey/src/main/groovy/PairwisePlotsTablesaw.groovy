import groovy.swing.SwingBuilder
import java.awt.Color
import smile.plot.*
import tech.tablesaw.api.*

import static javax.swing.JFrame.DISPOSE_ON_CLOSE as DISPOSE

def file = getClass().classLoader.getResource('whiskey.csv').file
def table = Table.read().csv(file)
//def table = Table.read().csv('whiskey.csv')
table = table.removeColumns(0)

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]

def panel = new PlotPanel(
        *[0..<cols.size(), 0..<cols.size()].combinations().collect { first, second ->
            def color = new Color(72 + (first * 16), 72 + (second * 16), 200 - (first * 4) - (second * 4))
            ScatterPlot.plot(table.as().doubleMatrix(cols[first], cols[second]), '#' as char, color)
        }
)

new SwingBuilder().edt {
    frame(title: 'Frame', size: [800, 600], show: true, defaultCloseOperation: DISPOSE) {
        widget(panel)
    }
}
