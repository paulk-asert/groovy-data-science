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
//@Grab('org.apache.tika:tika-parsers-standard-package:2.0.0')
import opennlp.tools.postag.*
import opennlp.tools.tokenize.SimpleTokenizer
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.pdf.PDFParser
import org.apache.tika.sax.BodyContentHandler

// use a helper to cache models
def helper = new ResourceHelper('http://opennlp.sourceforge.net/models-1.5')
def tokenizer = SimpleTokenizer.INSTANCE

def pdf = getClass().classLoader.getResource("PartsOfSpeech.pdf").file as File
pdf.withInputStream {is ->
    def handler = new BodyContentHandler()
    def metadata = new Metadata()
    def context = new ParseContext()
    def parser = new PDFParser()
    parser.parse(is, handler, metadata, context)

    // extract and tag content
    def sentences = handler.toString().readLines().grep()
    sentences.each {
        String[] tokens = tokenizer.tokenize(it)
        def model = new POSModel(helper.load('en-pos-maxent'))
        def posTagger = new POSTaggerME(model)
        String[] tags = posTagger.tag(tokens)
        println tokens.indices.collect{tags[it] == tokens[it] ? tags[it] : "${tags[it]}(${tokens[it]})" }.join(' ')
    }

    // extract some metadata
    def metadataOfInterest = ['dc:creator', 'Content-Type', 'pdf:encrypted', 'dc:title']
    def expectedMetadataValues = ['Paul King', 'application/pdf', 'false', 'POS.txt']
    assert metadataOfInterest.every{ it in metadata.names() }
    assert metadataOfInterest.collect{metadata.get(it) } == expectedMetadataValues
}
