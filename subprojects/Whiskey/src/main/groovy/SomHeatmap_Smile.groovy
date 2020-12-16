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

//def lattice = SOM.lattice(3, 3, data)
//def lattice = SOM.lattice(5, 4, data)
def lattice = SOM.lattice(7, 6, data)
//def lattice = SOM.lattice(8, 6, data)

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

def tooltip = { i, j -> names[[i, j]] ?: '' } as Hexmap.Tooltip
new Hexmap(model.umatrix(), Palette.jet(256), tooltip)
        .canvas().tap { setAxisLabels('', '') }.window()
