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
import java.time.LocalDateTime
import java.util.function.Function

import static java.lang.Math.sqrt
import static java.time.Month.JANUARY
import static tech.tablesaw.aggregate.AggregateFunctions.count
import static tech.tablesaw.aggregate.AggregateFunctions.countTrue
import static tech.tablesaw.api.BooleanColumn.create as bCol
import static tech.tablesaw.api.DoubleColumn.create as dCol
import static tech.tablesaw.api.StringColumn.create as sCol
import static tech.tablesaw.io.xlsx.XlsxReadOptions.builder

def url = getClass().classLoader.getResource('Scented_all.xlsx')
def helper = new TablesawHelper(url.file)
def table = new XlsxReader().read(builder(url).build())
def start2020 = LocalDateTime.of(2020, JANUARY, 1, 0, 0)
Function from2020 = r -> r.dateTimeColumn('Date').isAfter(start2020)

def candidates = ['[Nn]o scent', '[Nn]o smell', '[Dd]oes not smell like', "[Dd]oesn't smell like", "[Cc]an't smell",
                  '[Cc]annot smell', '[Ff]aint smell', '[Ff]aint scent', "[Dd]on't smell", '[Ll]ike nothing']
def monthNames = table.column('Date').collect { it.month.toString() }
def reviewFlags = table.column('Review').collect { review -> candidates.any { review =~ it } }
table.addColumns(sCol('Month', monthNames), bCol('Noscent', reviewFlags))

def byMonth2020 = table.where(from2020).sortAscendingOn('Date')
        .summarize('Noscent', countTrue, count).by('Month')
def indices = 0..<byMonth2020.size()
double[] nsprop = indices.collect { byMonth2020[it].with { it.getDouble('Number True [Noscent]') / it.getDouble('Count [Noscent]') } }
double[] se = indices.collect { sqrt(nsprop[it] * (1 - nsprop[it]) / byMonth2020[it].getDouble('Count [Noscent]')) }
double[] barLower = indices.collect { nsprop[it] - se[it] }
double[] barHigher = indices.collect { nsprop[it] + se[it] }
byMonth2020.addColumns(dCol('nsprop', nsprop), dCol('barLower', barLower), dCol('barHigher', barHigher))

def title = 'Proportion of top 5 scented candles on Amazon mentioning lack of scent by month 2020'
def layout = Layout.builder(title, 'Month', 'Proportion of reviews')
        .showLegend(false).width(1000).height(500).build()
def trace = BarTrace.builder(byMonth2020.categoricalColumn('Month'), byMonth2020.nCol('nsprop'))
        .orientation(BarTrace.Orientation.VERTICAL).opacity(0.5).build()
def errors = ScatterTrace.builder(byMonth2020.categoricalColumn('Month'), byMonth2020.nCol('barLower'),
        byMonth2020.nCol('barHigher'), byMonth2020.nCol('barLower'), byMonth2020.nCol('barHigher'))
        .type("candlestick").opacity(0.5).build()
helper.show(new Figure(layout, trace, errors), 'ReviewBarchart')
