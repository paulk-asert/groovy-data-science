//@Grab('de.lmu.ifi.dbs.elki:elki:0.7.5')
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansLloyd
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.KMeansPlusPlusInitialMeans
import de.lmu.ifi.dbs.elki.data.DoubleVector
import de.lmu.ifi.dbs.elki.data.NumberVector
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter
import de.lmu.ifi.dbs.elki.database.relation.Relation
import de.lmu.ifi.dbs.elki.datasource.FileBasedDatabaseConnection
import de.lmu.ifi.dbs.elki.datasource.parser.NumberVectorLabelParser
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction
import de.lmu.ifi.dbs.elki.utilities.random.RandomFactory

import static de.lmu.ifi.dbs.elki.data.type.TypeUtil.NUMBER_VECTOR_FIELD
import static de.lmu.ifi.dbs.elki.datasource.parser.CSVReaderFormat.DEFAULT_FORMAT

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]

def file = getClass().classLoader.getResource('whiskey.csv').file
def parser = new NumberVectorLabelParser(DEFAULT_FORMAT, [1] as long[], DoubleVector.FACTORY)
def dbc = new FileBasedDatabaseConnection([], parser, file)
def db = new StaticArrayDatabase(dbc, null)
db.initialize()
Relation<NumberVector> rel = db.getRelation(NUMBER_VECTOR_FIELD)
def ids = rel.DBIDs
def distilleries = new File(file).readLines().tail().collect{it.split(',')[1] }

def dist = EuclideanDistanceFunction.STATIC
def init = new KMeansPlusPlusInitialMeans(RandomFactory.DEFAULT)

def kmeans = new KMeansLloyd<NumberVector>(dist, 3, 0, init)
def c = kmeans.run(db)

def centroids = []
c.allClusters.eachWithIndex{clu, i ->
    def pts = clu.model.prototype.collect { sprintf '%.3f', it }
    centroids << "$clu.nameAutomatic$i: ${pts.join(', ')}"
    println "$clu.nameAutomatic$i, ${clu.size()} distilleries:"
    def names = []
    for (DBIDIter it = clu.IDs.iter(); it.valid(); it.advance()) {
        names << distilleries[ids.getOffset(it)]
    }
    println names.join(', ')
}
println '\nCentroids\nFeature:  ' + cols.join(', ')
centroids.each{ println it }