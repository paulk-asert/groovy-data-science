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

import org.apache.commons.math4.legacy.distribution.EmpiricalDistribution
import org.apache.commons.math4.legacy.stat.descriptive.SummaryStatistics

import static groovyx.javafx.GroovyFX.start
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

def file = getClass().classLoader.getResource('kc_house_data.csv').file
def csv  = CSV.withFirstRecordAsHeader().parse(new FileReader(file))
def all  = csv.findAll { it.bedrooms.toInteger() < 30 }.collect { it.price.toDouble() }
def info = new SummaryStatistics(); all.each(info::addValue)
def head = "Price percentile (min=\$$info.min, mean=\$${info.mean as int}, max=\$$info.max)"
def dist = EmpiricalDistribution.from(100, all as double[])
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
