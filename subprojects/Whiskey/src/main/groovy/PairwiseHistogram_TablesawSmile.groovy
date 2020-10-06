import smile.plot.Histogram3D
import smile.plot.PlotPanel
import tech.tablesaw.api.Table

import java.awt.Color

import static java.awt.Color.*

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

SwingUtil.show(size: [1200, 900], panel)
