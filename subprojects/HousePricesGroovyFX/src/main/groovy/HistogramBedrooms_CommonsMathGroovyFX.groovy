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
import org.apache.commons.math3.random.EmpiricalDistribution
import org.apache.commons.math3.stat.descriptive.SummaryStatistics

import static groovyx.javafx.GroovyFX.start
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

def full = getClass().classLoader.getResource('kc_house_data.csv').file
def csv  = CSV.withFirstRecordAsHeader().parse(new FileReader(full))
def all  = csv.collect { it.bedrooms.toInteger() }.findAll{ it < 30 }

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
