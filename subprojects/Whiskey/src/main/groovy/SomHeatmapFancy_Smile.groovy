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
import smile.math.MathEx
import smile.math.TimeFunction
import smile.plot.swing.*
import smile.vq.Neighborhood
import smile.vq.SOM

import static org.apache.commons.csv.CSVFormat.RFC4180 as CSV

def file = new File(getClass().classLoader.getResource('whiskey.csv').file)
def table = Read.csv(file.toPath(), CSV.withFirstRecordAsHeader())

String[] cols = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco', 'Honey',
                 'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']
def data = table.select(cols).toArray()
def distilleries = table.column('Distillery').toStringArray()

//def (ncols, nrows) = [3, 3]
//def (ncols, nrows) = [7, 4]
def (ncols, nrows) = [7, 6]
//def (ncols, nrows) = [8, 6]
def lattice = SOM.lattice(nrows, ncols, data)

int epochs = 100
def model = new SOM(lattice,
        TimeFunction.constant(0.1),
        Neighborhood.Gaussian(1, data.length * epochs / 8.0))
for (int i = 0; i < epochs; i++) {
    for (int j : MathEx.permutate(data.length)) {
        model.update(data[j])
    }
}

def groups = data.toList().indices.groupBy { idx -> group(model, data[idx]) }
def names = groups.collectEntries { k, v -> [k, distilleries[v].join(', ')] }

private group(SOM model, double[] row) {
    double[][][] neurons = model.neurons()
    [0..<neurons.size(), 0..<neurons[0].size()].combinations().min { pair ->
        def (i, j) = pair
        MathEx.distance(neurons[i][j], row)
    }
}

names.toSorted{ e1, e2 -> e1.key[0] <=> e2.key[0] ?: e1.key[1] <=> e2.key[1] }.each { k, v ->
    println "Cluster ${k[0]},${k[1]}: $v"
}

def lb, ub, sizeX, sizeY
def tooltip = { i, j -> names[[i, j]] ?: '' } as Hexmap.Tooltip
new Hexmap(model.umatrix(), Palette.terrain(64, 0.6f), tooltip).tap{
    lb = lowerBound
    ub = upperBound
    sizeX = (ub[0] - lb[0])/ncols*0.95
    sizeY = (ub[1] - lb[1])/nrows
}.canvas().tap {
    setAxisLabels('', '')
    groups.each{ k, v ->
        def evenRow = k[0] % 2 == 0
        double[] coords = [lb[0] + (evenRow ? 0.5 : 1) + k[1]  * sizeX, ub[1] - (k[0] + 0.5) * sizeY]
        add(Label.of(v.join(' '), coords))
    }
}.window()
