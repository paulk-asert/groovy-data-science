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

import de.uni_mannheim.minie.MinIE
import de.uni_mannheim.utils.coreNLP.CoreNLPUtils

def sentences = [
    'Paul has two sisters, Maree and Christine.',
    'No wise fish would go anywhere without a porpoise',
    'His bark was much worse than his bite',
    'The Groovy in Action book is a bargain at $50, or indeed any price.',
    'The conference wrapped up yesterday at 5:30 p.m. in Copenhagen, Denmark.',
    'I saw Ms. May Smith waving to June Jones.',
    'The parcel was passed from May to June.'
]

def parser = CoreNLPUtils.StanfordDepNNParser()
sentences.each { sentence ->
    def minie = new MinIE(sentence, parser, MinIE.Mode.SAFE)

    println "\nInput sentence: $sentence"
    println '============================='
    println 'Extractions:'
    for (ap in minie.propositions) {
        println "\tTriple: $ap.tripleAsString"
        def attr = ap.attribution.attributionPhrase ? ap.attribution.toStringCompact() : 'NONE'
        println "\tFactuality: $ap.factualityAsString\tAttribution: $attr"
        println '\t----------'
    }
}
