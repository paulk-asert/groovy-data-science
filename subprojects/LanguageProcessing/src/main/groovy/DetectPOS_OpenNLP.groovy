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
//@Grab('org.apache.opennlp:opennlp-tools:1.9.3')
import opennlp.tools.postag.*
import opennlp.tools.tokenize.SimpleTokenizer

// use a helper to cache models
def helper = new ResourceHelper('http://opennlp.sourceforge.net/models-1.5')
def sentences = [
    'Paul has two sisters, Maree and Christine.',
    'His bark was much worse than his bite',
    'Turn on the lights to the main bedroom',
    "Light 'em all up",
    'Make it dark downstairs'
]
def tokenizer = SimpleTokenizer.INSTANCE
sentences.each {
    String[] tokens = tokenizer.tokenize(it)
    def model = new POSModel(helper.load('en-pos-maxent'))
    def posTagger = new POSTaggerME(model)
    String[] tags = posTagger.tag(tokens)
    println tokens.indices.collect{tags[it] == tokens[it] ? tags[it] : "${tags[it]}(${tokens[it]})" }.join(' ')
}
