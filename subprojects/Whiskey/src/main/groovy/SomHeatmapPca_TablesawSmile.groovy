import java.awt.Color
import smile.plot.*
import smile.projection.PCA
import smile.vq.SOM
import tech.tablesaw.api.*
import static java.awt.Color.*

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

SwingUtil.show(new PlotPanel(scatter, hexmap))
