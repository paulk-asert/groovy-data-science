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
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.PTransform
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.values.PCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Log {
    private static final Logger LOGGER = LoggerFactory.getLogger(Log.class)
    private Log() { }

    static <T> PTransform<PCollection<T>, PCollection<T>> ofElements() {
        new LoggingTransform<>()
    }

    private static class LoggingTransform<T> extends PTransform<PCollection<T>, PCollection<T>> {
        @Override
        PCollection<T> expand(PCollection<T> input) {
            return input.apply(ParDo.of(new DoFn<T, T>() {
                @DoFn.ProcessElement
                void processElement(@DoFn.Element T element, DoFn.OutputReceiver<T> out) {
                    LOGGER.info(element.toString())
                    out.output(element)
                }
            }))
        }
    }
}
