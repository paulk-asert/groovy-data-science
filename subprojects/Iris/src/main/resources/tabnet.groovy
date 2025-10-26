import ai.djl.basicdataset.tabular.TabularDataset
import ai.djl.nn.Block
import ai.djl.zero.Performance
import weka.classifiers.djl.networkgenerator.NetworkGenerator
import weka.classifiers.djl.networkgenerator.TabularRegressionGenerator

class TabNetGenerator implements NetworkGenerator {
    Block generate(TabularDataset dataset) {
        new TabularRegressionGenerator(performance: Performance.FAST).generate(dataset)
    }
}
