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

import tech.tablesaw.api.*
import tech.tablesaw.plotly.api.*

import static tech.tablesaw.aggregate.AggregateFunctions.*

//def file = '/path/to/kc_house_data.csv' as File
def file = getClass().classLoader.getResource('kc_house_data.csv').file
def helper = new TablesawUtil(file)
Table rows = Table.read().csv(file)

println rows.shape()

println rows.structure()

println rows.column("bedrooms").summary().print()

println rows.where(rows.column("bedrooms").isGreaterThan(10))

def cleaned = rows.dropWhere(rows.column("bedrooms").isGreaterThan(30))
println cleaned.shape()
println cleaned.summarize("price", mean, min, max).by("bedrooms")

helper.show(ScatterPlot.create("Price x bathrooms x grade", cleaned, "bathrooms", "price", 'grade'), 'PriceBathroomsGrade')

cleaned.addColumns(
    StringColumn.create("waterfrontDesc", cleaned.column("waterfront").collect{ it ? 'waterfront' : 'interior' }),
    DoubleColumn.create("scaledGrade", cleaned.column("grade").collect{ it * 2 }),
    DoubleColumn.create("scaledPrice", cleaned.column("price").collect{ it / 100000 })
)

helper.show(BubblePlot.create("Price vs living area and grade (bubble size)",
        cleaned, "sqft_living", "price", "scaledGrade", "waterfrontDesc"), 'LivingPriceGradeWaterfront')

helper.show(Scatter3DPlot.create("Grade, living space, bathrooms and price (bubble size)",
        cleaned, "sqft_living", "bathrooms", "grade", "scaledPrice", "waterfrontDesc"), 'LivingBathroomsGradePriceWaterfront')
