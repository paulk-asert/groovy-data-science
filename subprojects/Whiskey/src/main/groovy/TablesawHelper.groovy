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

import tech.tablesaw.plotly.Plot
import tech.tablesaw.plotly.components.Figure

class TablesawHelper {
    private File parent

    /**
     * Creates the plot files in a suitable temporary location
     * determined from the parent of the passed file - typically
     * a build folder or IDE temporary folder.
     *
     * @param filename Of a file in a suitable temporary directory
     */
    TablesawHelper(String filename) {
        parent = new File(filename).parentFile
    }

    def show(Figure figure, String filename) {
        def file = new File(parent, filename + '.html')
        try {
            Plot.show(figure, file)
        } catch(ex) {
            println "Unable to show file '$file' due to '$ex.message'"
        }
    }
}
