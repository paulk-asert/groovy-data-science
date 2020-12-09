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
import com.opencsv.bean.*
import groovy.transform.ToString

@ToString(includeNames = true)
class House {
    @CsvBindByName
    Integer bedrooms

    @CsvBindByName
    String bathrooms

    @CsvBindByName(column = 'sqft_lot')
    Integer area_lot
}

def full = getClass().classLoader.getResource('kc_house_data.csv').file as File

def builder = new CsvToBeanBuilder(new FileReader(full))
def records = builder.withType(House).build().parse()

records.findAll{ it.bedrooms > 10 }.each{ println it }

/*
House(bedrooms:11, bathrooms:3, area_lot:4960)
House(bedrooms:33, bathrooms:1.75, area_lot:6000)
*/
