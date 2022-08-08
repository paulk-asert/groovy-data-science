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

import com.johnsnowlabs.nlp.DocumentAssembler
import com.johnsnowlabs.nlp.SparkNLP
import com.johnsnowlabs.nlp.annotators.Tokenizer
import com.johnsnowlabs.nlp.annotators.ner.NerConverter
import com.johnsnowlabs.nlp.annotators.ner.dl.NerDLModel
import com.johnsnowlabs.nlp.embeddings.WordEmbeddingsModel
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.PipelineStage
import org.apache.spark.sql.Encoders
import org.apache.spark.sql.Row

class DetectEntities_SparkNLP {

    static void main(String[] args) {

        var assembler = new DocumentAssembler(inputCol: 'text', outputCol: 'document', cleanupMode: 'disabled')

        var tokenizer = new Tokenizer(inputCols: ['document'] as String[], outputCol: 'token')

        var embeddings = WordEmbeddingsModel.pretrained('glove_100d').tap {
            inputCols = ['document', 'token'] as String[]
            outputCol = 'embeddings'
        }

        var model = NerDLModel.pretrained('onto_100', 'en').tap {
            inputCols = ['document', 'token', 'embeddings'] as String[]
            outputCol ='ner'
        }

        var converter = new NerConverter(inputCols: ['document', 'token', 'ner'] as String[], outputCol: 'ner_chunk')

        var pipeline = new Pipeline(stages: [assembler, tokenizer, embeddings, model, converter] as PipelineStage[])

        var spark = SparkNLP.start(false, false, '16G', '', '', '')

        var text = [
            "The Mona Lisa is a 16th century oil painting created by Leonardo. It's held at the Louvre in Paris."
        ]
        var data = spark.createDataset(text, Encoders.STRING()).toDF('text')

        var pipelineModel = pipeline.fit(data)

        var transformed = pipelineModel.transform(data)
        transformed.show()

        use(SparkCategory) {
            transformed.collectAsList().each { row ->
                var res = row.text
                var chunks = row.ner_chunk.reverseIterator()
                while (chunks.hasNext()) {
                    var chunk = chunks.next()
                    int begin = chunk.begin
                    int end = chunk.end
                    var entity = chunk.metadata.get('entity').get()
                    res = res[0..<begin] + "$entity($chunk.result)" + res[end<..-1]
                }
                println res
            }
        }
        println "\nFinished running Spark NLP NER example with GROOVY"
    }

    static class SparkCategory {
        static get(Row r, String field) { r.get(r.fieldIndex(field)) }
    }
}
