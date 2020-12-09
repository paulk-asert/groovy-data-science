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
import java.awt.Color
import smile.plot.*
import tech.tablesaw.api.*

def file = getClass().classLoader.getResource('whiskey.csv').file
def table = Table.read().csv(file)
//def table = Table.read().csv('whiskey.csv')
table = table.removeColumns(0)

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]

def panel = new PlotPanel(
        *[0..<cols.size(), 0..<cols.size()].combinations().collect { first, second ->
            def color = new Color(72 + (first * 16), 72 + (second * 16), 200 - (first * 4) - (second * 4))
            ScatterPlot.plot(table.as().doubleMatrix(cols[first], cols[second]), '#' as char, color)
        }
)

SwingUtil.show(size: [1200, 900], panel)
