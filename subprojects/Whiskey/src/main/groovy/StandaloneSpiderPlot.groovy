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
import static JFreeChartUtil.*

def cols = ['Body', 'Sweetness', 'Smoky', 'Medicinal', 'Tobacco', 'Honey',
            'Spicy', 'Winey', 'Nutty', 'Malty', 'Fruity', 'Floral']

def data = [
    [2.7037, 2.4444, 1.4074, 0.0370, 0.0000, 1.8519, 1.6667, 1.8519, 1.8889, 2.0370, 2.1481, 1.6667],
    [1.8500, 1.9000, 2.0000, 0.9500, 0.1500, 1.1000, 1.5000, 0.6000, 1.5500, 1.7000, 1.3000, 1.5000],
    [1.2667, 2.1333, 0.9333, 0.1333, 0.0000, 1.0667, 0.8000, 0.5333, 1.8000, 1.7333, 2.2667, 2.2667],
    [3.6667, 1.5000, 3.6667, 3.3333, 0.6667, 0.1667, 1.6667, 0.5000, 1.1667, 1.3333, 1.1667, 0.1667],
    [1.5000, 2.8889, 1.0000, 0.2778, 0.1667, 1.0000, 1.2222, 0.6111, 0.5556, 1.7778, 1.6667, 2.0000]
]
def centroids = categoryDataset()
data.eachWithIndex { nums, i ->
    nums.eachWithIndex { val, j -> centroids.addValue(val, "Cluster ${i + 1}", cols[j]) }
}

def centroidPlot = spiderWebPlot(dataset: centroids)
def centroidChart = chart('Centroid spider plot', centroidPlot)

SwingUtil.showH(centroidChart, size: [400, 400], title: 'Whiskey clusters')
