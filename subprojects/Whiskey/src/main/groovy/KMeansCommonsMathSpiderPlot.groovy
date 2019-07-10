import groovy.swing.SwingBuilder
import org.apache.commons.math3.ml.clustering.*
import org.jfree.chart.*
import org.jfree.chart.plot.SpiderWebPlot
import org.jfree.data.category.DefaultCategoryDataset

import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

def file = getClass().classLoader.getResource('whiskey.csv').file
def rows = CSV.withFirstRecordAsHeader().parse(new FileReader(file))

def cols = ["Body", "Sweetness", "Smoky", "Medicinal",
            "Tobacco", "Honey", "Spicy", "Winey",
            "Nutty", "Malty", "Fruity", "Floral"]

def groups = new KMeansPlusPlusClusterer(5)
def data = rows.collect{ row -> new DoublePoint(cols.collect{ col -> row[col] } as int[]) }
def centroids = groups.cluster(data)
println cols.join(', ')
def dataset = new DefaultCategoryDataset()
centroids.eachWithIndex{ ctrd, num ->
    def pt = ctrd.center.point
    println pt.collect{ sprintf '%.3f', it }.join(', ')
    pt.eachWithIndex { val, idx -> dataset.addValue(val, "Cluster ${num+1}", cols[idx]) }
}

def plot = new SpiderWebPlot(dataset: dataset)
def chart = new JFreeChart('Whiskey clusters', plot)
def panel = new ChartPanel(chart)

new SwingBuilder().edt {
    frame(title: 'Frame', size: [600, 600], show: true) {
        widget(panel)
    }
}
