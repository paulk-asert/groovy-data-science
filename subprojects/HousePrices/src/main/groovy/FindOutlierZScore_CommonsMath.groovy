//@Grab('org.apache.commons:commons-math3:3.6.1')
//@Grab('org.apache.commons:commons-csv:1.8')
import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

//def file = 'kc_house_data.csv' as File
def file = getClass().classLoader.getResource('kc_house_data.csv').file
def records = CSV.withFirstRecordAsHeader().parse(new FileReader(file)).toList()
def exclusions = [:].withDefault{[]}
records.each{ if (it.bedrooms == '0') exclusions.zero_bedrooms += it.recordNumber }
records.each{ if (it.bathrooms == '0') exclusions.zero_bathrooms += it.recordNumber }
def augmented = records
        .findAll{it.recordNumber !in exclusions.values().sum() }
        .collect {
            def bdb = (it.bedrooms.toDouble() / it.bathrooms.toDouble()).toDouble()
            it.toMap() + [recordNumber: it.recordNumber, bed_div_bath: bdb] }

def values = augmented.collect{it.bed_div_bath } as double[]
def ds = new DescriptiveStatistics(values)
println 'Num bedrooms divided by num bathrooms ' + ds
double mean = ds.mean

def dist = new NormalDistribution()
double variance = ds.populationVariance
double sd = Math.sqrt(variance)
for (int idx = 0; idx < ds.N; idx++) {
    def zscore = (ds.getElement(idx) - mean) / sd
    augmented[idx].zscore = zscore
    augmented[idx].surv = 1.0 - dist.cumulativeProbability(zscore.abs())
}
println augmented.findAll{it.zscore > 10 || it.surv == 0 }.join('\n')
augmented.each{if (it.zscore > 10) exclusions.bad_zscore += it.recordNumber }
println '\nRecommended exclusions:\n' + exclusions
