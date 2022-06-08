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
//@Grab('org.apache.nlpcraft:nlpcraft:0.9.0')
//@Grab('org.apache.nlpcraft:nlpcraft-example-lightswitch:0.9.0')

import org.apache.nlpcraft.NCStart
import groovy.ant.AntBuilder
import org.apache.nlpcraft.examples.lightswitch.*
import org.apache.nlpcraft.model.tools.test.NCTestClientBuilder

import static org.apache.nlpcraft.model.tools.embedded.NCEmbeddedProbe.start
import static org.apache.nlpcraft.model.tools.embedded.NCEmbeddedProbe.stop

def t = Thread.start { new AntBuilder().with {
    java(classname: NCStart.name, fork: true, clonevm: true) {
        arg(value: '-server')
        // uncomment if needed if you downloaded the all jar as per instructions in Lights.gradle build file
//        classpath {
//            fileset(dir: 'lib') {
//                include(name: '**/*.jar')
//            }
//            pathelement(location: "lib/apache-nlpcraft-incubating-0.8.0-all-deps.jar")
//        }
    }
}}
sleep 45000 // allow server to start up
def models = [
        java: LightSwitchJavaModel,
        groovy: LightSwitchGroovyModel,
        scala: LightSwitchScalaModel,
        kotlin: LightSwitchKotlinModel
]

List<String> names = models.values()*.name
start(null, names)

models.keySet().each { key ->
    def cli = new NCTestClientBuilder().newBuilder().build()
    cli.open("nlpcraft.lightswitch.ex.$key")
    println cli.ask('Turn on the lights in the master bedroom')
    println cli.ask("Light 'em all up")
    println cli.ask('Make it dark downstairs') // expecting no match
    if (cli) {
        cli.close()
    }
}

stop()
t.interrupt()
t.join()
