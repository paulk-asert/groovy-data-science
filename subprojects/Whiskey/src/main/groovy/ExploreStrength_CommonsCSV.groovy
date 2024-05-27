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
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

def file = getClass().classLoader.getResource('whiskey.csv').file
def parser = CSV.builder().setHeader().setSkipHeaderRecord(true).build()
def records = parser.parse(new FileReader(file))
def rows = records.collectEntries{ row -> [row[1], row.toList()[2..13]] }
def zeros = rows.collectEntries{ e -> [e.key, e.value.count{ it == '0' }] }
def fours = rows.collectEntries{ e -> [e.key, e.value.count{ it == '4' }] }
def maxZeros = zeros.values().toSet().max()
def maxFours = fours.values().toSet().max()
println "Distinctively flavored (most zeros): ${zeros.findAll{ e -> e.value == maxZeros }*.key.join(', ')}"
println "Powerfully flavored (most fours): ${fours.findAll{ e -> e.value == maxFours }*.key.join(', ')}"
/*
Distinctively flavored (most zeros): Glenfiddich
Powerfully flavored (most fours): Ardbeg, Lagavulin, Laphroig
*/
