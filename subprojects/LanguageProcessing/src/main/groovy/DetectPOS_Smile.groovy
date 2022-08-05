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
import smile.nlp.pos.HMMPOSTagger
import smile.nlp.tokenizer.SimpleTokenizer

def sentences = [
    'Paul has two sisters, Maree and Christine.',
    'No wise fish would go anywhere without a porpoise',
    'His bark was much worse than his bite',
    'Turn on the lights to the main bedroom',
    "Light 'em all up",
    'Make it dark downstairs'
]
def tokenizer = new SimpleTokenizer(true)
sentences.each {
    def tokens = Arrays.stream(tokenizer.split(it)).toArray(String[]::new)
    def tags = HMMPOSTagger.default.tag(tokens)*.toString()
    println tokens.indices.collect{tags[it] == tokens[it] ? tags[it] : "${tags[it]}(${tokens[it]})" }.join(' ')
}
