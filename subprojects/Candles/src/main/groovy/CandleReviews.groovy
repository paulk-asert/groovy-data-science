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

import tech.tablesaw.io.xlsx.XlsxReader
import tech.tablesaw.plotly.components.Figure
import tech.tablesaw.plotly.components.Layout
import tech.tablesaw.plotly.traces.BarTrace
import tech.tablesaw.plotly.traces.ScatterTrace
import tech.tablesaw.plotly.Plot
import java.time.LocalDateTime

import static java.lang.Math.sqrt
import static java.time.Month.JANUARY
import static tech.tablesaw.aggregate.AggregateFunctions.count
import static tech.tablesaw.aggregate.AggregateFunctions.countTrue
import static tech.tablesaw.api.BooleanColumn.create as bCol
import static tech.tablesaw.api.DoubleColumn.create as dCol
import static tech.tablesaw.api.StringColumn.create as sCol
import static tech.tablesaw.io.xlsx.XlsxReadOptions.builder
import static tech.tablesaw.plotly.traces.BarTrace.Orientation.VERTICAL

var url = getClass().classLoader.getResource('Scented_all.xlsx')
var table = new XlsxReader().read(builder(url).build())

var monthCol = sCol('Month', table.column('Date').collect {
    it.month.toString()
})
var candidates = ['[Nn]o scent', '[Nn]o smell', '[Ff]aint smell',
    '[Ff]aint scent', "[Cc]an't smell", '[Dd]oes not smell like',
    "[Dd]oesn't smell like", '[Cc]annot smell', "[Dd]on't smell",
    '[Ll]ike nothing']
var noScentCol = bCol('Noscent', table.column('Review').collect {
    review -> candidates.any { review =~ it }
})

table.addColumns(monthCol, noScentCol)

var start2020 = LocalDateTime.of(2020, JANUARY, 1, 0, 0)
var byMonth2020 = table
    .where(r -> r.dateTimeColumn('Date').isAfter(start2020))
    .sortAscendingOn('Date')
    .summarize('Noscent', countTrue, count)
    .by('Month')

double[] nsprop = byMonth2020.collect {
    it.getDouble('Number True [Noscent]') /
    it.getDouble('Count [Noscent]')
}

var indices = 0..<byMonth2020.size()
double[] se = indices.collect {
    sqrt(nsprop[it] * (1 - nsprop[it]) /
    byMonth2020[it].getDouble('Count [Noscent]')) }
double[] barLower = indices.collect { nsprop[it] - se[it] }
double[] barHigher = indices.collect { nsprop[it] + se[it] }

byMonth2020.addColumns(dCol('nsprop', nsprop),
                       dCol('barLower', barLower),
                       dCol('barHigher', barHigher))

var title = 'Proportion of top 5 scented candles on Amazon mentioning lack of scent by month 2020'
var layout = Layout.builder(title, 'Month',
    'Proportion of reviews')
    .showLegend(false).width(1000).height(500).build()
var trace = BarTrace.builder(
    byMonth2020.categoricalColumn('Month'),
    byMonth2020.nCol('nsprop'))
    .orientation(VERTICAL).opacity(0.5).build()
var errors = ScatterTrace.builder(
    byMonth2020.categoricalColumn('Month'),
    byMonth2020.nCol('barLower'), byMonth2020.nCol('barHigher'),
    byMonth2020.nCol('barLower'), byMonth2020.nCol('barHigher'))
    .type("candlestick").opacity(0.5).build()

//var helper = new TablesawHelper(url.file)
//helper.show(new Figure(layout, trace, errors), 'ReviewBarchart')
var chart = new Figure(layout, trace, errors)
var parentDir = new File(url.file).parentFile
Plot.show(chart, new File(parentDir, 'ReviewBarchart.html'))
