import org.encog.ml.data.basic.BasicMLData
import org.encog.ml.data.basic.BasicMLDataSet
import org.encog.ml.kmeans.KMeansClustering
import org.encog.util.csv.CSVFormat
import org.encog.util.csv.ReadCSV

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]

def file = getClass().classLoader.getResource('whiskey.csv').file
def csv = new ReadCSV(file, true, CSVFormat.EG_FORMAT)
def set = new BasicMLDataSet()
while (csv.next()) {
    set.add(new BasicMLData(cols.collect{col -> csv.getDouble(col) } as double[]))
}

def clusterer = new KMeansClustering(3, set)
clusterer.iteration(100)

clusterer.clusters.eachWithIndex { cluster, idx ->
    println "Cluster $idx: "
    cluster.createDataSet().each {
        println it.inputArray
    }
}
