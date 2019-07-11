import groovy.swing.SwingBuilder

import java.awt.Color
import smile.plot.*
import tech.tablesaw.api.Table
import static java.awt.Color.*
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE as DISPOSE

def file = getClass().classLoader.getResource('whiskey.csv').file
def table = Table.read().csv(file)
//def table = Table.read().csv('whiskey.csv')
table = table.removeColumns(0)

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]

Color[] colors = [CYAN, PINK, MAGENTA, ORANGE, GREEN, BLUE, RED, YELLOW]

def panel = new PlotPanel(
        *[cols, cols].combinations().collect { first, second ->
            Histogram3D.plot(table.as().doubleMatrix(first, second), 4, colors)
        }
)

new SwingBuilder().edt {
    frame(title: 'Frame', size: [1200, 900], show: true, defaultCloseOperation: DISPOSE) {
        widget(panel)
    }
}

SwingUtil.show(size: [1200, 900], panel)
