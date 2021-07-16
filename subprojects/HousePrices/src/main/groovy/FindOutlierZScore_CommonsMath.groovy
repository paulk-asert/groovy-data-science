/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
println '\nRecommended exclusions:\n' + exclusions.entrySet().join('\n')
