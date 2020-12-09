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
def full = getClass().classLoader.getResource('kc_house_data.csv').file as File
def parent = full.parentFile
def lines = full.readLines()
println lines.size()

def (trainLines, testLines) = lines.chop(lines.size() * 0.8 as int, -1)

def train = new File(parent, 'house_train.csv')
train.text = trainLines.join('\n')
println train.readLines().size()

def test = new File(parent, 'house_test.csv')
test.delete()
test << lines[0] << '\n' << testLines.join('\n')
println test.readLines().size()
