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
import nlp4j.impl.DefaultDocument
import nlp4j.krmj.annotator.KuromojiAnnotator
import nlp4j.stanford.StanfordPosAnnotator

var doc = new DefaultDocument()
doc.putAttribute('text', 'I eat sushi with chopsticks.')
var ann = new StanfordPosAnnotator()
ann.setProperty('target', 'text')
ann.annotate(doc)
println doc.keywords.collect{  k -> "${k.facet - 'word.'}(${k.str})" }.join(' ')

doc = new DefaultDocument()
doc.putAttribute('text', '私は学校に行きました。')
ann = new KuromojiAnnotator()
ann.setProperty('target', 'text')
ann.annotate(doc)
println doc.keywords.collect{ k -> "${k.facet}(${k.str})" }.join(' ')
