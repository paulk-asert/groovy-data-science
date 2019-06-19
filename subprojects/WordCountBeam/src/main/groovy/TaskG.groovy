import org.apache.beam.sdk.transforms.Count
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.DoFn.Element
import org.apache.beam.sdk.transforms.DoFn.OutputReceiver
import org.apache.beam.sdk.transforms.DoFn.ProcessElement
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.transforms.ProcessFunction
import org.apache.beam.sdk.values.KV
import org.apache.beam.sdk.values.PCollection

import static org.apache.beam.sdk.transforms.FlatMapElements.into
import static org.apache.beam.sdk.values.TypeDescriptors.strings

class TaskG {
    static PCollection applyTransform(PCollection input) {
        ProcessFunction asWords = line -> line.split(" ").toList()

        def kv2out = new DoFn<KV, String>() {
            @ProcessElement
            void processElement(@Element KV element, OutputReceiver<String> out) {
                out.output(element.key + ":" + element.value)
            }
        }

        return input
                .apply(into(strings()).via(asWords))
                .apply(Count.perElement())
                .apply(ParDo.of(kv2out))
    }
}
