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
import com.fasterxml.jackson.annotation.JsonIncludeProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema

import static com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.IGNORE_TRAILING_UNMAPPABLE

@JsonPropertyOrder(['bedrooms', 'bathrooms', 'sqft_lot'])
@JsonIncludeProperties(['bedrooms', 'bathrooms', 'sqft_lot'])
record HouseRecord(Integer bedrooms, String bathrooms, Integer sqft_lot/*, @JsonProperty("sqft_lot") Integer area_lot*/) { }

def full = getClass().classLoader.getResource('kc_house_data.csv').file as File

var schema = CsvSchema.builder()
        .addColumn("id")
        .addColumn("date")
        .addColumn("price")
        .addColumn("bedrooms")
        .addColumn("bathrooms")
        .addColumn("sqft_living")
        .addColumn("sqft_lot")
        .build()
        .withHeader()

def mapper = new CsvMapper().configure(IGNORE_TRAILING_UNMAPPABLE, true)
def records = mapper.readerFor(HouseRecord).with(schema).readValues(full).readAll()

records.findAll{ it.bedrooms > 10 }.each{ println it }

/*
HouseRecord[bedrooms=11, bathrooms=3, sqft_lot=4960]
HouseRecord[bedrooms=33, bathrooms=1.75, sqft_lot=6000]
*/
