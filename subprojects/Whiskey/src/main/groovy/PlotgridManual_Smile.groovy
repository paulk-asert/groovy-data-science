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
import java.awt.Font

import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

def file = new File(getClass().classLoader.getResource('whiskey.csv').file)
def table = Read.csv(file.toPath(), CSV.withFirstRecordAsHeader())

def cols = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco', 'Honey',
            'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']
table = table.select(*cols)
char mark = '#'

new PlotGrid(
        *[cols, cols].combinations().collect { first, second ->
            def (f, s) = [cols.indexOf(first), cols.indexOf(second)]
            def color = new Color(72 + (f * 16), 72 + (s * 16), 200 - (f * 4) - (s * 4))
            ScatterPlot.of(table, first, second, mark, color).canvas().tap {
                margin = 0.28
                setAxisLabels('', '')
                title = first.take(6) + ' x ' + second.take(6)
                titleColor = Color.DARK_GRAY
                titleFont = new Font("Arial", Font.ITALIC, 12)
            }.panel()
        }
).window()
