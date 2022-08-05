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
//@Grab('org.apache.opennlp:opennlp-tools:2.0.0')
import opennlp.tools.namefind.*
import opennlp.tools.tokenize.SimpleTokenizer
import opennlp.tools.util.*

String[] sentences = [
    "A commit by Daniel Sun on December 6, 2020 improved Groovy 4's language integrated query.",
    "A commit by Daniel on Sun., December 6, 2020 improved Groovy 4's language integrated query.",
    'The Groovy in Action book by Dierk Koenig et. al. is a bargain at $50, or indeed any price.',
    'The conference wrapped up yesterday at 5:30 p.m. in Copenhagen, Denmark.',
    'I saw Ms. May Smith waving to June Jones.',
    'The parcel was passed from May to June.',
    'The Mona Lisa by Leonardo da Vinci has been on display in the Louvre, Paris since 1797.'
]

def base = 'http://opennlp.sourceforge.net/models-1.5'
def modelNames = ['person', 'money', 'date', 'time', 'location']
def finders = modelNames.collect { model ->
    new NameFinderME(DownloadUtil.downloadModel(new URL("$base/en-ner-${model}.bin"), TokenNameFinderModel))
}

def tokenizer = SimpleTokenizer.INSTANCE
sentences.each { sentence ->
    String[] tokens = tokenizer.tokenize(sentence)
    Span[] tokenSpans = tokenizer.tokenizePos(sentence)
    def entityText = [:]
    def entityPos = [:]
    finders.indices.each {fi ->
        // could be made smarter by looking at probabilities and overlapping spans
        Span[] spans = finders[fi].find(tokens)
        spans.each{span ->
            def se = span.start..<span.end
            def pos = (tokenSpans[se.from].start)..<(tokenSpans[se.to].end)
            entityPos[span.start] = pos
            entityText[span.start] = "$span.type(${sentence[pos]})"
        }
    }
    entityPos.keySet().sort().reverseEach {
        def pos = entityPos[it]
        def (from, to) = [pos.from, pos.to + 1]
        sentence = sentence[0..<from] + entityText[it] + sentence[to..-1]
    }
    println sentence
}
