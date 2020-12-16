//@Grab('org.apache.nlpcraft:nlpcraft:0.7.2')

import org.apache.nlpcraft.NCStart
import groovy.ant.AntBuilder
import org.apache.nlpcraft.examples.lightswitch.LightSwitchModel
import org.apache.nlpcraft.model.tools.test.NCTestClientBuilder

import static org.apache.nlpcraft.model.tools.embedded.NCEmbeddedProbe.start
import static org.apache.nlpcraft.model.tools.embedded.NCEmbeddedProbe.stop

def t = Thread.start { new AntBuilder().with {
    java(classname: NCStart.name, fork: true, clonevm: true) {
        arg(value: '-server')
        // uncomment if needed if you downloaded the all jar as per instructions in Lights.gradle build file
//        classpath {
//            pathelement(location: "lib/apache-nlpcraft-incubating-0.7.2-all-deps.jar")
//        }
    }
}}
sleep 35000 // allow server to start up

start(LightSwitchModel)

def cli = new NCTestClientBuilder().newBuilder().build()

cli.open("nlpcraft.lightswitch.ex")
println cli.ask('Turn on the lights in the main bedroom')
println cli.ask("Light 'em all up")
println cli.ask('Make it dark downstairs') // expecting no match
if (cli) {
    cli.close()
}

stop()
t.interrupt()
t.join()
