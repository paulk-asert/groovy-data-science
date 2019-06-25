//@Grab('org.groovyfx:groovyfx:8.0.0')
//@Grab('org.apache.commons:commons-csv:1.7')
//@Grab('org.apache.commons:commons-math3:3.6.1')
import org.apache.commons.math3.random.EmpiricalDistribution
import org.apache.commons.math3.stat.descriptive.SummaryStatistics

import static groovyx.javafx.GroovyFX.start
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

//println System.getProperty('user.dir')
def full = getClass().classLoader.getResource('kc_house_data.csv').file
//def full = '../resources/kc_house_data.csv' as File
def csv  = CSV.withFirstRecordAsHeader().parse(new FileReader(full))
def all  = csv.collect { it.bedrooms.toInteger() }//.findAll{ it < 30 }

def stats = new SummaryStatistics()
all.each{ stats.addValue(it as double) }
println stats.summary

def dist = new EmpiricalDistribution(all.max()).tap{ load(all as double[]) }
def bins = dist.binStats.withIndex().collectMany { v, i -> [i.toString(), v.n] }

start {
  stage(title: 'Number of bedrooms histogram', show: true, width: 800, height: 600) {
    scene {
      barChart(title: 'Bedroom count', barGap: 0, categoryGap: 2) {
        series(name: 'Number of properties', data: bins)
      }
    }
  }
}
