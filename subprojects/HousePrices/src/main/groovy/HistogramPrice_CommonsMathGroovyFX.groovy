import org.apache.commons.math3.random.EmpiricalDistribution
import org.apache.commons.math3.stat.descriptive.SummaryStatistics

import static groovyx.javafx.GroovyFX.start
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

def file = getClass().classLoader.getResource('kc_house_data.csv').file
//def file = '/path/to/kc_house_data.csv' as File
def csv  = CSV.withFirstRecordAsHeader().parse(new FileReader(file))
def all  = csv.findAll { it.bedrooms.toInteger() < 30 }.collect { it.price.toDouble() }
def info = new SummaryStatistics(); all.each(info::addValue)
def head = "Price percentile (min=\$$info.min, mean=\$${info.mean as int}, max=\$$info.max)"
def dist = new EmpiricalDistribution(100).tap{ load(all as double[]) }
def bins = dist.binStats.withIndex().collectMany { v, i -> [i.toString(), v.n] }
//println info
start {
  stage(title: 'Price histogram', show: true, width: 800, height: 600) {
    scene {
      barChart(title: head, barGap: 0, categoryGap: 0) {
        series(name: 'Number of properties', data: bins)
      }
    }
  }
}
