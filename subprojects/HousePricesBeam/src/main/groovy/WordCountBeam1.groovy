import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.TextIO
import org.apache.beam.sdk.transforms.Count
import org.apache.beam.sdk.transforms.Regex
import org.apache.beam.sdk.transforms.ToString
import util.Log

def p = Pipeline.create()
p.apply(TextIO.read().from('file://path/to/peppers.txt'))
        .apply(Regex.split(/\W+/))
        .apply(Count.perElement())
        .apply(ToString.elements())
        .apply(Log.ofElements())
p.run()
