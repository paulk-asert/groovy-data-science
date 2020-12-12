<!--
SPDX-License-Identifier: Apache-2.0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Natural Language Processing

Natural language processing may involve language detection, parsing, part-of-speech tagging and other activities.
We'll look at just a few of those and focus on using pre-trained models.
Groovy code examples can be found in the [src/main/groovy](src/main/groovy) directory
and make use of the [Apache OpenNLP](https://opennlp.apache.org/) library.

## Language Detection

This example makes use of a pre-trained language model to detect the language used for a fragment of text.
It uses the [Apache OpenNLP](https://opennlp.apache.org/) library.

```groovy
import opennlp.tools.langdetect.*

def base = 'http://apache.forsale.plus/opennlp/models'
def url = "$base/langdetect/1.8.3/langdetect-183.bin"
def model = new LanguageDetectorModel(new URL(url))
def detector = new LanguageDetectorME(model)

def languages = [
    'spa': 'Bienvenido a Madrid',
    'fra': 'Bienvenue à Paris',
    'dan': 'Velkommen til København',
    'bul': 'Добре дошли в София'
]

languages.each { k, v ->
    assert detector.predictLanguage(v).lang == k
}
```
If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

Alternatively, you can run the example online using a Jupyter/Beakerx notebook:
[![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/paulk-asert/groovy-data-science/master?filepath=subprojects%2FLanguageProcessing%2Fsrc%2Fmain%2Fnotebook%2FLanguageProcessing.ipynb)

## Sentence Detection

OpenNLP also supports sentence detection. We load the trained sentence detection model
for English and use that to process some text. Even though the text has 28 full stops,
only 4 of them are associated with the end of a sentence.

```groovy
import opennlp.tools.sentdetect.*

def text = '''
The most referenced scientific paper of all time is "Protein measurement with the
Folin phenol reagent" by Lowry, O. H., Rosebrough, N. J., Farr, A. L. & Randall,
R. J. and was published in the J. BioChem. in 1951. It describes a method for
measuring the amount of protein (even as small as 0.2 ?, were ? is the specific
weight) in solutions and has been cited over 300,000 times and can be found here:
https://www.jbc.org/content/193/1/265.full.pdf. Dr. Lowry completed
two doctoral degrees under an M.D.-Ph.D. program from the University of Chicago
before moving to Harvard under A. Baird Hastings. He was also the H.O.D of
Pharmacology at Washington University in St. Louis for 29 years.
'''

def url = 'http://opennlp.sourceforge.net/models-1.5/en-sent.bin'
def model = new SentenceModel(new URL(url))
def detector = new SentenceDetectorME(model)
def sentences = detector.sentDetect(text)
assert text.count('.') == 28
assert sentences.size() == 4
println sentences.join('\n\n')
```

Running the script yields the following output:
```asciidoc
The most referenced scientific paper of all time is "Protein measurement with the
Folin phenol reagent" by Lowry, O. H., Rosebrough, N. J., Farr, A. L. & Randall,
R. J. and was published in the J. BioChem. in 1951.

It describes a method for
measuring the amount of protein (even as small as 0.2 γ, were γ is the specific
weight) in solutions and has been cited over 300,000 times and can be found here:
https://www.jbc.org/content/193/1/265.full.pdf.

Dr. Lowry completed
two doctoral degrees under an M.D.-Ph.D. program from the University of Chicago
before moving to Harvard under A. Baird Hastings.

He was also the H.O.D of
Pharmacology at Washington University in St. Louis for 29 years.
```

## Entity Detection

```groovy
import opennlp.tools.namefind.*
import opennlp.tools.tokenize.SimpleTokenizer
import opennlp.tools.util.Span

String[] sentences = [
    "A commit by Daniel Sun on December 6, 2020 improved Groovy 4's language integrated query.",
    "A commit by Daniel on Sun., December 6, 2020 improved Groovy 4's language integrated query.",
    'The Groovy in Action book by Dierk Koenig et. al. is a bargain at $50, or indeed any price.',
    'The conference wrapped up yesterday at 5:30 p.m. in Copenhagen, Denmark.',
    'I saw Ms. May Smith waving to June Jones.',
    'The parcel was passed from May to June.'
]

// use a helper to cache models
def helper = new ResourceHelper('http://opennlp.sourceforge.net/models-1.5')

def modelNames = ['person', 'money', 'date', 'time', 'location']
def finders = modelNames.collect{ new NameFinderME(new TokenNameFinderModel(helper.load("en-ner-$it"))) }

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
    entityPos.keySet().toList().reverseEach {
        def pos = entityPos[it]
        def (from, to) = [pos.from, pos.to + 1]
        sentence = sentence[0..<from] + entityText[it] + sentence[to..-1]
    }
    println sentence
}
```

Gives the following result:

```asciidoc
A commit by person(Daniel Sun) on date(December 6, 2020) improved Groovy 4's language integrated query.
A commit by person(Daniel) on Sun., date(December 6, 2020) improved Groovy 4's language integrated query.
The Groovy in Action book by person(Dierk Koenig) et. al. is a bargain at money($50), or indeed any price.
The conference wrapped up date(yesterday) at time(5:30 p.m.) in location(Copenhagen), location(Denmark).
I saw Ms. person(May Smith) waving to person(June Jones).
The parcel was passed from date(May to June).
```

The result isn't perfect.
Arguably "Sun." should be detected as part of the subsequent `date` in the first sentence,
and "Ms." as part of the `person` entity in the 5th sentence.
Also, the `location` entities in sentence 4 could potentially be merged.
We can either do fancier programming or train better models to help for such cases.

## Scaling up natural language processing

We have all seen recent advancements in systems like Apple's Siri,
Google Assistant and Amazon Alexa. This is part of a bigger push towards
Natural Language Interface (NLI) interaction with computers.
Such systems can be quite complex, yet the expectation is that
folks using simple devices will be able to access them.
The ability to deliver such services depends on smart scaling approaches.

The [Apache NLPCraft](https://nlpcraft.apache.org/) (incubating) project
provides a scalable architecture for the supporting natural language interface
approach with language models as code, flexible components and good
third party integration.

![NLPCraft architecture](https://nlpcraft.apache.org/images/homepage-fig1.1.png)

Groovy code examples can be found in the [LanguageProcessingNLPCraft/src/main/groovy](../../subprojects/LanguageProcessingNLPCraft/src/main/groovy) directory.
This example is interacting with one of the built-in example language models
for light switches:

```groovy
start(LightSwitchModel)

def cli = new NCTestClientBuilder().newBuilder().build()

cli.open("nlpcraft.lightswitch.ex")
println cli.ask('Turn on the lights in the master bedroom')
println cli.ask("Light 'em all up")
println cli.ask('Make it dark downstairs') // expecting no match
if (cli) {
    cli.close()
}

stop()
```

Running the example gives the following output:
```asciidoc
Lights are [on] in [master bedroom].
Lights are [on] in [entire house].
No matching intent found.
```

We could now revise our model if we wanted the last example to also succeed.
