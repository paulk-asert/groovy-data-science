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
import edu.stanford.nlp.simple.Document

def doc = new Document('''
StanfordNLP is fantastic!
Groovy is great fun!
Math can be hard!
''')
for (sent in doc.sentences()) {
    println "${sent.toString().padRight(40)} ${sent.sentiment()}"
}
