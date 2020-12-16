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
import smile.plot.swing.Histogram3D
import smile.plot.swing.PlotGrid
import java.awt.Color

import static java.awt.Color.*
import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

def file = new File(getClass().classLoader.getResource('whiskey.csv').file)
def table = Read.csv(file.toPath(), CSV.withFirstRecordAsHeader())

String[] cols = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco', 'Honey',
                 'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']

Color[] colors = [CYAN, PINK, MAGENTA, ORANGE, GREEN, BLUE, RED, YELLOW]

new PlotGrid(
        *[cols, cols].combinations().collect { first, second ->
            def f = table.column(first).toDoubleArray()
            def s = table.column(second).toDoubleArray()
            Histogram3D.of([f, s].transpose() as double[][], 4, colors)
        }*.canvas()*.panel()
).window()
