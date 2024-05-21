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
import static org.apache.commons.math4.legacy.stat.StatUtils.sumSq
import static smile.math.MathEx.dot

var features = [
        'price', 'bedrooms', 'bathrooms', 'sqft_living', 'sqft_living15', 'lat',
        'sqft_above', 'grade', 'view', 'waterfront', 'floors'
]

var file = new File(getClass().classLoader.getResource('kc_house_data.csv').file)
var format = CSV.builder().setHeader().setSkipHeaderRecord(true).build()
var table = Read.csv(file.toPath(), format)
table = table.select(*features)
var filtered = table.toList().findAll { it.apply('bedrooms') <= 30 }
table = DataFrame.of(filtered)
println table.schema()
println table.structure()

GParsPool.withPool {
    var trainChunkSize = 3000
    var numTrainChunks = 8
    var models = (0..<numTrainChunks).collectParallel {
        var list = DataFrame.of(table.toArray().toList().shuffled().take(trainChunkSize) as double[][], *features)
        var model = OLS.fit(Formula.lhs('price'), list)
        [model.intercept(), *model.coefficients()]
    }
    models.eachWithIndex { m, i ->
        println "Model for chunk $i:"
        println "Intercept = ${m[0]}\nCoefficients = ${m[1..-1]}"
    }
    var model = models.transpose()*.sum().collect { it / numTrainChunks }
    println 'Merged model:'
    println "Intercept = ${model[0]}\nCoefficients = ${model[1..-1]}"

    double[] coefficients = model[1..-1]
    double intercept = model[0]
    var stats = { chunk ->
        var predicted = chunk.collect { row -> intercept + dot(row[1..-1] as double[], coefficients) }
        var residuals = chunk.toList().indexed().collect { idx, row -> predicted[idx] - row[0] }
        var rmse = sqrt(sumSq(residuals as double[]) / chunk.size())
        [rmse, residuals.average(), chunk.size()]
    }
//    println stats(data)
    var evalChunkSize = 2000
    var results = table.toArray().collate(evalChunkSize).collectParallel(stats)
    println 'RMSE: ' + sqrt(results.collect { it[0] * it[0] * it[2] }.sum() / table.size())
    println 'mean: ' + results.collect { it[1] * it[2] }.sum() / table.size()
}
