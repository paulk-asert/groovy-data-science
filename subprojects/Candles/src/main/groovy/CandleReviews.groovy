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
import tech.tablesaw.io.xlsx.XlsxReader
import tech.tablesaw.plotly.Plot
import tech.tablesaw.plotly.components.Figure
import tech.tablesaw.plotly.components.Layout
import tech.tablesaw.plotly.traces.BarTrace
import tech.tablesaw.plotly.traces.ScatterTrace
import tech.tablesaw.selection.Selection

import java.time.LocalDateTime
import java.util.function.Function

import static java.time.Month.JANUARY
import static tech.tablesaw.aggregate.AggregateFunctions.count
import static tech.tablesaw.aggregate.AggregateFunctions.countTrue
import static tech.tablesaw.io.xlsx.XlsxReadOptions.builder

def url = getClass().classLoader.getResource('Scented_all.xlsx')
def table = new XlsxReader().read(builder(url).build())
def start2020 = LocalDateTime.of(2020, JANUARY, 1, 0, 0)
Function<Table, Selection> from2020 = r -> r.dateTimeColumn('Date').isAfter(start2020)

def candidates = [
        '[Nn]o scent', '[Nn]o smell', '[Dd]oes not smell like', "[Dd]oesn't smell like", "[Cc]an't smell",
        '[Cc]annot smell', '[Ff]aint smell', '[Ff]aint scent', "[Dd]on't smell", '[Ll]ike nothing']
def adjusted = table.addColumns(
        StringColumn.create('Month', table.column('Date').collect { it.month.toString() }),
        BooleanColumn.create('Noscent', table.column('Review').collect {review -> candidates.any{ review =~ it } })
)
def only2020 = adjusted.where(from2020)
        .sortAscendingOn('Date')
        .summarize('Noscent', countTrue, count).by('Month')
double[] nsprop = (0..<only2020.size()).collect{only2020[it].with{it.getDouble('Number True [Noscent]')/it.getDouble('Count [Noscent]') } }
double[] se = (0..<only2020.size()).collect{Math.sqrt(nsprop[it] * (1 - nsprop[it]) / only2020[it].getDouble('Count [Noscent]')) }
double[] barLower = (0..<only2020.size()).collect{nsprop[it] - se[it] }
double[] barHigher = (0..<only2020.size()).collect{nsprop[it] + se[it] }
def next = only2020.addColumns(
        DoubleColumn.create('nsprop', nsprop),
        DoubleColumn.create('barLower', barLower),
        DoubleColumn.create('barHigher', barHigher)
)

def layout = Layout.builder("Proportion of top 5 scented candles on Amazon mentioning lack of scent by month 2020", 'Month', 'Proportion of reviews')
        .showLegend(false).width(1000).height(500).build()
println barLower
println barHigher
println next.last(12)
def trace = BarTrace.builder(next.categoricalColumn('Month'), next.numberColumn('nsprop'))
        .orientation(BarTrace.Orientation.VERTICAL)
        .opacity(0.5)
        .build()
def errors = ScatterTrace.builder(next.categoricalColumn('Month'), next.numberColumn('barLower'),
        next.numberColumn('barHigher'), next.numberColumn('barLower'), next.numberColumn('barHigher'))
        .type("candlestick")
        .opacity(0.5)
        .build()
Plot.show(new Figure(layout, trace, errors))
