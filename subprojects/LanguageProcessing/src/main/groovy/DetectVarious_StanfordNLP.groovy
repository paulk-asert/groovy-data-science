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

import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.pipeline.StanfordCoreNLP

import static edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import static edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
import static edu.stanford.nlp.naturalli.NaturalLogicAnnotations.RelationTriplesAnnotation
import static edu.stanford.nlp.semgraph.SemanticGraph.OutputFormat.LIST
import static edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation

def text = '''
Paul has two sisters, Maree and Christine.
No wise fish would go anywhere without a porpoise.
His bark was much worse than his bite.
The Groovy in Action book is a bargain at $50, or indeed any price.
The conference wrapped up yesterday at 5:30 p.m. in Copenhagen, Denmark.
I saw Ms. May Smith waving to June Jones.
The parcel was passed from May to June.
'''

def props = [annotators: 'tokenize,ssplit,pos,lemma,depparse,natlog,openie'] as Properties
def pipeline = new StanfordCoreNLP(props)

def doc = new Annotation(text)
pipeline.annotate(doc)

int sentNo = 0
for (sentence in doc.get(SentencesAnnotation)) {
    println "\nSentence #${++sentNo}: ${sentence.get(TextAnnotation)}"

    println sentence.get(EnhancedDependenciesAnnotation).toString(LIST)

    def triples = sentence.get(RelationTriplesAnnotation)
    if (triples) {
        println 'Triples:'
        for (triple in triples) {
            triple.with {
                println "$confidence\t${subjectGloss()}\t${relationGloss()}\t${objectGloss()}"
            }
        }
    }
}
