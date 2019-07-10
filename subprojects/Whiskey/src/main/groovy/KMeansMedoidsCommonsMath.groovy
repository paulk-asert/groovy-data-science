import groovy.swing.SwingBuilder
import org.apache.commons.math3.ml.clustering.*
import org.jfree.chart.*
import org.jfree.chart.plot.SpiderWebPlot
import org.jfree.data.category.DefaultCategoryDataset

import static javax.swing.JFrame.DISPOSE_ON_CLOSE as DISPOSE
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV
import static org.apache.commons.math3.stat.StatUtils.sumSq

def file = getClass().classLoader.getResource('whiskey.csv').file
def rows = CSV.withFirstRecordAsHeader().parse(new FileReader(file)).toList()

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]

def kmeans = new KMeansPlusPlusClusterer(5)
def data = rows.collect{ row -> new DoublePoint(cols.collect{ col -> row[col] } as int[]) }
def centroids = kmeans.cluster(data)
println cols.join(', ') + ', Medoid'
def dataset = new DefaultCategoryDataset()
centroids.eachWithIndex{ ctrd, num ->
    def cpt = ctrd.center.point
    def closest = ctrd.points.min{ pt ->
        sumSq((0..<cpt.size()).collect{ cpt[it] - pt.point[it] } as double[])
    }
    def medoid = rows.find{ row -> cols.collect{ row[it] as double } == closest.point }?.Distillery
    println cpt.collect{ sprintf '%.3f', it }.join(', ') + ", $medoid"
    cpt.eachWithIndex { val, idx -> dataset.addValue(val, "Cluster ${num+1}", cols[idx]) }
}

def plot = new SpiderWebPlot(dataset: dataset)
def chart = new JFreeChart('Whiskey clusters', plot)
def panel = new ChartPanel(chart)

new SwingBuilder().edt {
    frame(title: 'Frame', size: [600, 600], show: true, defaultCloseOperation: DISPOSE) {
        widget(panel)
    }
}
