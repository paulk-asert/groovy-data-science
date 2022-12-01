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

import org.apache.poi.xssf.usermodel.XSSFWorkbook

var url = getClass().classLoader.getResource('Scented_all.xlsx')
var table = []
var candidates = ['[Nn]o scent', '[Nn]o smell', '[Ff]aint smell',
    '[Ff]aint scent', "[Cc]an't smell", '[Dd]oes not smell like',
    "[Dd]oesn't smell like", '[Cc]annot smell', "[Dd]on't smell",
    '[Ll]ike nothing']
url.withInputStream { ins ->
    new XSSFWorkbook(ins).getSheetAt(0).eachWithIndex { row, idx ->
        if (idx > 0) { // skip header
            var date = row.getCell(1).dateCellValue
            var month = date.format('MMM')
            var review = row.getCell(3)
            var noscent = candidates.any { review =~ it }
            table << [NoScent: noscent, Date: date, Month: month]
        }
    }
}

var start2020 = Date.parse('dd-MMM-yyyy', '01-Jan-2020')
println GQL {
    from row in table
    where row.Date > start2020
    groupby row.Month
    orderby row.Date
    select row.Month, agg(_g.toList().count{ it.row.NoScent }) / count(row.Date)
}.join('\n')
