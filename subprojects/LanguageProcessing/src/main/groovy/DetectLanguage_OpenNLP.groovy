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
//@Grab('org.apache.opennlp:opennlp-tools:2.0.0') // or 1.9.4 on JDK8
import opennlp.tools.langdetect.*

// use a helper to cache models
def helper = new ResourceHelper('https://dlcdn.apache.org/opennlp/models/langdetect/1.8.3/')
def model = new LanguageDetectorModel(helper.load('langdetect-183'))
def detector = new LanguageDetectorME(model)

[ 'spa': 'Bienvenido a Madrid',
  'fra': 'Bienvenue à Paris',
  'dan': 'Velkommen til København',
  'bul': 'Добре дошли в София'
].each { k, v ->
    assert detector.predictLanguage(v).lang == k
}
