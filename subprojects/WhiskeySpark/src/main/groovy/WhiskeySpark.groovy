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
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.feature.VectorAssembler

import static org.apache.spark.sql.SparkSession.builder

static main(args) {
    var spark = builder().config('spark.master', 'local[8]').appName('Whiskey').orCreate
    spark.sparkContext().logLevel = 'WARN' // quieten logging once we've started
    var file = WhiskeySpark.classLoader.getResource('whiskey.csv').file

    var rows = spark.read().format('com.databricks.spark.csv')
            .options(header: 'true', inferSchema: 'true').load(file)
    String[] colNames = rows.columns().toList() - ['RowID', 'Distillery']
    var assembler = new VectorAssembler(inputCols: colNames, outputCol: 'features')
    var dataset = assembler.transform(rows)
    var kmeans = new KMeans(k: 5, seed: 1L)
    var model = kmeans.fit(dataset)
    println '\nCluster centers:'
    model.clusterCenters().each { println it.values().collect { sprintf '%.2f', it }.join(', ') }
    println()
    var result = model.transform(dataset)
    var clusters = result.toLocalIterator().collect { row ->
        [row.getAs('prediction'), row.getAs('Distillery')]
    }.groupBy { it[0] }.collectValues { it*.get(1) }
    clusters.each { k, v -> println "Cluster$k: ${v.join(', ')}"}
    println()
    spark.sparkContext().logLevel = 'INFO' // logging back to normal
    spark.stop()
}
/*
24/05/26 10:55:38 INFO SparkContext: Running Spark version 3.5.1
...
Cluster centers:
2.89, 2.42, 1.53, 0.05, 0.00, 1.84, 1.58, 2.11, 2.11, 2.11, 2.26, 1.58
1.45, 2.35, 1.06, 0.26, 0.06, 0.84, 1.13, 0.45, 1.26, 1.65, 2.19, 2.10
1.83, 3.17, 1.00, 0.33, 0.17, 1.00, 0.67, 0.83, 0.83, 1.50, 0.50, 1.50
3.00, 1.50, 3.00, 2.80, 0.50, 0.30, 1.40, 0.50, 1.50, 1.50, 1.30, 0.50
1.85, 2.20, 1.70, 0.40, 0.10, 1.85, 1.80, 1.00, 1.35, 2.00, 1.40, 1.85

Cluster0: Aberfeldy, Aberlour, Auchroisk, Balmenach, BenNevis, Benrinnes, BlairAthol, Dailuaine, Dalmore, Edradour, Glendronach, Glendullan, Glenfarclas, Glenrothes, Longmorn, Macallan, Mortlach, RoyalLochnagar, Strathisla
Cluster1: AnCnoc, Auchentoshan, Aultmore, Balblair, Benriach, Bladnoch, Bunnahabhain, Cardhu, Craigganmore, Dufftown, GlenElgin, GlenGrant, GlenKeith, GlenMoray, Glenallachie, Glenfiddich, Glengoyne, Glenkinchie, Glenlossie, Glenmorangie, Linkwood, Loch Lomond, Mannochmore, RoyalBrackla, Speyside, Strathmill, Tamdhu, Tamnavulin, Teaninich, Tobermory, Tullibardine
Cluster3: Ardbeg, Caol Ila, Clynelish, GlenScotia, Isle of Jura, Lagavulin, Laphroig, Oban, OldPulteney, Talisker
Cluster4: Ardmore, Belvenie, Benromach, Bowmore, Bruichladdich, Craigallechie, Dalwhinnie, Deanston, GlenGarioch, GlenOrd, Glenlivet, Glenturret, Highland Park, Inchgower, Knochando, OldFettercairn, Scapa, Springbank, Tomatin, Tomintoul
Cluster2: ArranIsleOf, GlenDeveronMacduff, GlenSpey, Miltonduff, Speyburn, Tomore
...
24/05/26 10:55:51 INFO SparkContext: Successfully stopped SparkContext
*/
