import java.util.Arrays;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.FlatMapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import util.Log;

public class Task {
    static PCollection<String> applyTransform(PCollection<String> input) {
        return input
                .apply(FlatMapElements.into(TypeDescriptors.strings())
                        .via(line -> Arrays.asList(line.split(" "))))
                .apply(Count.perElement())
                .apply(ParDo.of(new DoFn<KV<String, Long>, String>() {
                    @ProcessElement
                    public void processElement(
                            @Element KV<String, Long> element, OutputReceiver<String> out) {
                        out.output(element.getKey() + ":" + element.getValue());
                    }
                }));
    }
}
