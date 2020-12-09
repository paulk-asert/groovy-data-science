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
import smile.plot.Histogram3D
import smile.plot.PlotPanel
import tech.tablesaw.api.Table

import java.awt.Color

import static java.awt.Color.*

def file = getClass().classLoader.getResource('whiskey.csv').file
def table = Table.read().csv(file)
//def table = Table.read().csv('whiskey.csv')
table = table.removeColumns(0)

def cols = ["Body", "Sweetness", "Smoky", "Medicinal", "Tobacco", "Honey",
            "Spicy", "Winey", "Nutty", "Malty", "Fruity", "Floral"]

Color[] colors = [CYAN, PINK, MAGENTA, ORANGE, GREEN, BLUE, RED, YELLOW]

def panel = new PlotPanel(
        *[cols, cols].combinations().collect { first, second ->
            Histogram3D.plot(table.as().doubleMatrix(first, second), 4, colors)
        }
)

SwingUtil.show(size: [1200, 900], panel)
