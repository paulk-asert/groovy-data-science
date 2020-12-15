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
import smile.io.Read
import smile.plot.swing.PlotGrid
import smile.plot.swing.ScatterPlot
import java.awt.Color

import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

def file = new File(getClass().classLoader.getResource('whiskey.csv').file)
def table = Read.csv(file.toPath(), CSV.withFirstRecordAsHeader())

String[] cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
                 "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]
table = table.select(cols)

new PlotGrid(
        *[0..<cols.size(), 0..<cols.size()].combinations().collect { first, second ->
            def color = new Color(72 + (first * 16), 72 + (second * 16), 200 - (first * 4) - (second * 4))
            def f = table.column(first).toDoubleArray()
            def s = table.column(second).toDoubleArray()
            ScatterPlot.of([f, s].transpose() as double[][], '#' as char, color)
        }*.canvas()*.panel()
).window()
