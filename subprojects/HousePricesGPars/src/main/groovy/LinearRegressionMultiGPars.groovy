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
import groovyx.gpars.GParsPool
import smile.data.DataFrame
import smile.data.formula.Formula
import smile.io.Read
import smile.regression.OLS

import static java.lang.Math.sqrt
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV
import static org.apache.commons.math3.stat.StatUtils.sumSq
import static smile.math.MathEx.dot

def features = [
        'price', 'bedrooms', 'bathrooms', 'sqft_living', 'sqft_living15', 'lat',
        'sqft_above', 'grade', 'view', 'waterfront', 'floors'
]

def file = new File(getClass().classLoader.getResource('kc_house_data.csv').file)
def table = Read.csv(file.toPath(), CSV.withFirstRecordAsHeader())
table = table.select(*features)
table = table.stream().filter { it.apply('bedrooms') <= 30 }.collect(DataFrame.collect())
println table.schema()
println table.structure()

GParsPool.withPool {
    def trainChunkSize = 3000
    def numTrainChunks = 8
    def models = (0..<numTrainChunks).collectParallel {
        def list = DataFrame.of(table.toArray().toList().shuffled().take(trainChunkSize) as double[][], *features)
        OLS.fit(Formula.lhs('price'), list).coefficients()
    }
    models.each {
        println "Intercept: ${it[0]}, Coefficients: ${it[1..-1]}"
    }
    def model = models.transpose()*.sum().collect { it / numTrainChunks }
    println "Intercept: ${model[0]}"
    println "Coefficients: ${model[1..-1].join(', ')}"

    double[] coefficients = model[1..-1]
    double intercept = model[0]
    def stats = { chunk ->
        def predicted = chunk.collect { row -> intercept + dot(row[1..-1] as double[], coefficients) }
        def residuals = chunk.toList().indexed().collect { idx, row -> predicted[idx] - row[0] }
        def rmse = sqrt(sumSq(residuals as double[]) / chunk.size())
        [rmse, residuals.average(), chunk.size()]
    }
//    println stats(data)
    def evalChunkSize = 2000
    def results = table.toArray().collate(evalChunkSize).collectParallel(stats)
    println 'RMSE: ' + sqrt(results.collect { it[0] * it[0] * it[2] }.sum() / table.size())
    println 'mean: ' + results.collect { it[1] * it[2] }.sum() / table.size()
}
