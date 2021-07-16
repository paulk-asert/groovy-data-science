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

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.dataformat.csv.CsvDataFormat
import org.apache.camel.impl.DefaultCamelContext

import static org.apache.camel.language.groovy.GroovyLanguage.groovy

def file = getClass().classLoader.getResource('kc_house_data.csv').file

class DisplayHouseProcessor implements Processor {
    void process(Exchange exchange) throws Exception {
        println exchange.in.body
    }
}

def uri = new File(file).parentFile.toURI().toString() + '?fileName=kc_house_data.csv&noop=true'
def context = new DefaultCamelContext()
context.addRoutes(new RouteBuilder() {
    void configure() {
        def csv = new CsvDataFormat(useMaps: true)
        from(uri)
                .unmarshal(csv)
                .split(body())
                .streaming()
                .filter(groovy("request.body.bedrooms.toInteger() > 10"))
                .process(new DisplayHouseProcessor())
    }
})
context.start()
sleep 10000
context.stop()

/*
INFO: Apache Camel 3.11.0 (camel-1) started in 211ms (build:49ms init:149ms start:13ms)
[id:1773100755, date:20140821T000000, price:520000, bedrooms:11, bathrooms:3, sqft_living:3000, sqft_lot:4960,
 floors:2, waterfront:0, view:0, condition:3, grade:7, sqft_above:2400, sqft_basement:600, yr_built:1918,
 yr_renovated:1999, zipcode:98106, lat:47.556, long:-122.363, sqft_living15:1420, sqft_lot15:4960]
[id:2402100895, date:20140625T000000, price:640000, bedrooms:33, bathrooms:1.75, sqft_living:1620, sqft_lot:6000,
 floors:1, waterfront:0, view:0, condition:5, grade:7, sqft_above:1040, sqft_basement:580, yr_built:1947,
 yr_renovated:0, zipcode:98103, lat:47.6878, long:-122.331, sqft_living15:1330, sqft_lot15:4700]
*/
