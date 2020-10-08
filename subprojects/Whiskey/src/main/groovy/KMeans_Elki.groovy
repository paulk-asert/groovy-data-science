//@Grab('de.lmu.ifi.dbs.elki:elki:0.7.5')
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansLloyd
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter
import de.lmu.ifi.dbs.elki.datasource.FileBasedDatabaseConnection
import de.lmu.ifi.dbs.elki.datasource.parser.NumberVectorLabelParser
import de.lmu.ifi.dbs.elki.result.AutomaticVisualization
import de.lmu.ifi.dbs.elki.utilities.ELKIBuilder
//import de.lmu.ifi.dbs.elki.visualization.VisualizerParameterizer

def cols = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco', 'Honey',
            'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']

def file = getClass().classLoader.getResource('whiskey.csv').file
def parser = new ELKIBuilder(NumberVectorLabelParser).with('parser.labelIndices', '0,1').build()
def dbc = new FileBasedDatabaseConnection([], parser, file)
def db = new StaticArrayDatabase(dbc, null)
db.initialize()

def kmeans = new ELKIBuilder(KMeansLloyd).with('kmeans.k', 4).build()
def c = kmeans.run(db)

def auto = new ELKIBuilder(AutomaticVisualization)
        .with(AutomaticVisualization.Parameterizer.WINDOW_TITLE_ID, 'Whiskey clusters')
        //.with(VisualizerParameterizer.Parameterizer.ENABLEVIS_ID, 'scatter|parallel|key')
        .build()

def hier = db.hierarchy
hier.add(db, c)
auto.processNewResult(hier, c)

def centroids = []
c.allClusters.eachWithIndex{clu, i ->
    def pts = clu.model.prototype.collect { sprintf '%.3f', it }
    centroids << "$clu.nameAutomatic$i:  ${pts.join(', ')}"
    println "$clu.nameAutomatic$i, ${clu.size()} distilleries:"
    def names = []
    for (DBIDIter it = clu.IDs.iter(); it.valid(); it.advance()) {
        names << db.getBundle(it).data(2) // labels
    }
    println names.join(', ')
}
println '\nCentroids: ' + cols.join(', ')
centroids.each{ println it }
