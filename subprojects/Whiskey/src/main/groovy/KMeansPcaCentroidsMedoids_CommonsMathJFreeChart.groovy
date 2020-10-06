import org.apache.commons.math3.linear.EigenDecomposition
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.ml.clustering.DoublePoint
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer
import org.apache.commons.math3.stat.correlation.Covariance
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.SpiderWebPlot
import org.jfree.chart.plot.XYPlot
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.DefaultXYZDataset

import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV
import static org.apache.commons.math3.stat.StatUtils.sumSq

def file = getClass().classLoader.getResource('whiskey.csv').file
def rows = CSV.withFirstRecordAsHeader().parse(new FileReader(file))

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]

def clusterer = new KMeansPlusPlusClusterer(4)
List<DoublePoint> data = []
List<String> distilleries = []
Map<Integer, List> clusterPts = [:]
rows.each{ row ->
    data << new DoublePoint(cols.collect{ col -> row[col] } as int[])
    distilleries << row.Distillery
}
def clusters = clusterer.cluster(data)
println cols.join(', ')
def centroids = new DefaultCategoryDataset()
clusters.eachWithIndex{ ctrd, num ->
    def cpt = ctrd.center.point
    clusterPts[num] = ctrd.points.collect{ pt -> data.point.findIndexOf{ it == pt.point } }
    println cpt.collect{ sprintf '%.3f', it }.join(', ')
    cpt.eachWithIndex { val, idx -> centroids.addValue(val, "Cluster ${num+1}", cols[idx]) }
}

println "\n${cols.join(', ')}, Medoid"
def medoids = new DefaultCategoryDataset()
clusters.eachWithIndex{ ctrd, num ->
    def cpt = ctrd.center.point
    def closest = ctrd.points.min{ pt ->
        sumSq((0..<cpt.size()).collect{ cpt[it] - pt.point[it] } as double[])
    }
    def medoidIdx = data.findIndexOf{ row -> row.point == closest.point }
    println data[medoidIdx].point.collect{ sprintf '%.3f', it }.join(', ') + ", ${distilleries[medoidIdx]}"
    data[medoidIdx].point.eachWithIndex { val, idx -> medoids.addValue(val, distilleries[medoidIdx], cols[idx]) }
}

def centroidPlot = new SpiderWebPlot(dataset: centroids)
def centroidChart = new JFreeChart('Centroid spider plot', centroidPlot)

def medoidPlot = new SpiderWebPlot(dataset: medoids)
def medoidChart = new JFreeChart('Medoid spider plot', medoidPlot)

def pointsArray = data*.point as double[][]
def mean = (0..<pointsArray[0].length).collect{col ->
    (0..<pointsArray.length).collect{ row ->
        pointsArray[row][col]
    }.average()
} as double[]
(0..<pointsArray[0].length).collect{col ->
    (0..<pointsArray.length).collect{ row ->
        pointsArray[row][col] -= mean[col]
    }.average()
}
def realMatrix = MatrixUtils.createRealMatrix(pointsArray)

// calculate PCA by hand: create covariance matrix of points, then find eigen vectors
// see https://stats.stackexchange.com/questions/2691/making-sense-of-principal-component-analysis-eigenvectors-eigenvalues

def covariance = new Covariance(realMatrix)
def covarianceMatrix = covariance.covarianceMatrix
def ed = new EigenDecomposition(covarianceMatrix)
double[] eigenValues = ed.realEigenvalues
//def solver = ed.solver
def principalComponents = MatrixUtils.createRealMatrix(eigenValues.length, clusterer.k)
for (int i = 0; i < clusterer.k; i++) {
    for (int j = 0; j < eigenValues.length; j++) {
        principalComponents.setEntry(j, i, ed.getEigenvector(i).getEntry(j))
    }
}

def xyz = new DefaultXYZDataset()
def transformed = realMatrix.multiply(principalComponents)

clusterPts.each{ k, v ->
    def (x, y, z) = [[], [], []]
    v.each { idx ->
        x << -transformed.getEntry(idx, 0)
        y << transformed.getEntry(idx, 1)
        z << -(transformed.getEntry(idx, 2))// - zmin)/(zmax - zmin)*2
    }
    xyz.addSeries("Cluster ${k+1}:", [x, y, z] as double[][])
}

def xaxis = new NumberAxis(label: "PCA1", autoRange: false, lowerBound: -3.5, upperBound: 7)
def yaxis = new NumberAxis(label: "PCA2", autoRange: false, lowerBound: -6, upperBound: 4)
def bubblePlot = new XYPlot(xyz, xaxis, yaxis, JFreeChartUtil.bubbleRenderer())
def bubbleChart = new JFreeChart('PCA bubble plot', bubblePlot)
def bubblePanel = new ChartPanel(bubbleChart)

SwingUtil.showH(new ChartPanel(centroidChart), new ChartPanel(medoidChart), bubblePanel,
        size: [1000, 400],
        title: 'Whiskey clusters: CSV=commons-csv kmeans,PCA=commons-math plot=jfreechart'
)
