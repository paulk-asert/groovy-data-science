//@Grab('org.apache.beam:beam-sdks-java-core:2.13.0')
//@Grab('org.apache.beam:beam-runners-direct-java:2.13.0')
//@Grab('org.slf4j:slf4j-api:1.7.26')
//@Grab('org.slf4j:slf4j-jdk14:1.7.26')

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.transforms.Count
import org.apache.beam.sdk.transforms.Create
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.DoFn.Element
import org.apache.beam.sdk.transforms.DoFn.OutputReceiver
import org.apache.beam.sdk.transforms.DoFn.ProcessElement
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.transforms.ProcessFunction
import org.apache.beam.sdk.values.KV
import org.apache.beam.sdk.values.PCollection
import util.Log

import static org.apache.beam.sdk.transforms.FlatMapElements.into
import static org.apache.beam.sdk.values.TypeDescriptors.strings

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

def lines = ["apple orange grape banana apple banana",
             "banana orange banana papaya"]

def pipeline = Pipeline.create()
def counts   = pipeline.apply(Create.of(lines))
def output   = applyTransform(counts)

output.apply(Log.ofElements())

pipeline.run()

/*
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {

  private static final Logger LOGGER = LoggerFactory.getLogger(Log.class);

  private Log() {
  }

  public static <T> PTransform<PCollection<T>, PCollection<T>> ofElements() {
    return new LoggingTransform<>();
  }

  public static <T> PTransform<PCollection<T>, PCollection<T>> ofElements(String prefix) {
    return new LoggingTransform<>(prefix);
  }

  private static class LoggingTransform<T> extends PTransform<PCollection<T>, PCollection<T>> {

    private String prefix;

    private LoggingTransform() {
      prefix = "";
    }

    private LoggingTransform(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public PCollection<T> expand(PCollection<T> input) {
      return input.apply(ParDo.of(new DoFn<T, T>() {

        @ProcessElement
        public void processElement(@Element T element, OutputReceiver<T> out) {
          LOGGER.info(prefix + element.toString());

          out.output(element);
        }

      }));
    }

  }

}
*/