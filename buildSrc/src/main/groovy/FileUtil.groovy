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
class FileUtil {
    private FileUtil() {}

    static List<String> baseNames(File subdir, List<String> exclusions = [], String ext = '.groovy') {
        baseNames(subdir.listFiles().toList(), exclusions, [ext])
    }

    static List<String> baseNames(Collection<File> files, List<String> exclusions = [], List<String> exts = ['.groovy']) {
        exts.collect { ext ->
            files*.name
                    .findAll { it.endsWith(ext) }
                    .collect { it - ext }
                    .findAll { !(it in exclusions) && !it.endsWith('Util') }
        }.sum()
    }
}
