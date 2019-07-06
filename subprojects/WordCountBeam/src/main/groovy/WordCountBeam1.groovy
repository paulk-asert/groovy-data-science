//@Grab('org.apache.beam:beam-sdks-java-core:2.13.0')
//@Grab('org.apache.beam:beam-runners-direct-java:2.13.0')
//@Grab('org.slf4j:slf4j-api:1.7.26')
//@Grab('org.slf4j:slf4j-jdk14:1.7.26')

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
