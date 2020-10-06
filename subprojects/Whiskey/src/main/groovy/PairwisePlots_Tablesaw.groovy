import java.awt.Color
import smile.plot.*
import tech.tablesaw.api.*

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

SwingUtil.show(size: [1200, 900], panel)
