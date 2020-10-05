import com.datumbox.framework.core.common.dataobjects.Dataframe
import com.datumbox.framework.common.Configuration
import com.datumbox.framework.core.machinelearning.MLBuilder
import com.datumbox.framework.core.machinelearning.clustering.Kmeans

import static com.datumbox.framework.common.dataobjects.TypeInference.DataType.NUMERICAL
import static com.datumbox.framework.common.dataobjects.TypeInference.DataType.CATEGORICAL
import static com.datumbox.framework.core.machinelearning.clustering.Kmeans.TrainingParameters.Distance.EUCLIDIAN
import static com.datumbox.framework.core.machinelearning.clustering.Kmeans.TrainingParameters.Initialization.FORGY

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]

def config = Configuration.configuration
def headers = [*:cols.collectEntries{[it, NUMERICAL] }, Distillery: CATEGORICAL]
Dataframe df
new File(getClass().classLoader.getResource('whiskey.csv').file).withReader {
    df = Dataframe.Builder.parseCSVFile(it, 'Distillery', headers, ',' as char, '"' as char, "\r\n", null, null, config)
}

def param = new Kmeans.TrainingParameters(k: 3, maxIterations: 200, initializationMethod: FORGY,
        distanceMethod: EUCLIDIAN, weighted: false, categoricalGamaMultiplier: 1.0, subsetFurthestFirstcValue: 2.0)

def clusterer = MLBuilder.create(param, config)
clusterer.fit(df)
clusterer.predict(df)
def clusters = [:].withDefault{[]}
df.entries().each{ e ->
    clusters["Cluster-$e.value.YPredicted"] << e.value.y
}
df.delete()
println clusters.sort{e -> e.key }.entrySet().join('\n')
