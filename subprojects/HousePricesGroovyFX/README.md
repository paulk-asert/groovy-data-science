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

This project logically belongs in the [HousePrices](../HousePrices/)
project but has been split out to make it easier to
manage the requirements for [GroovyFX](http://groovyfx.org/).

<img src="../../docs/images/cottage.png" height="250" alt="house"/>

## GroovyFX overview

[GroovyFX](http://groovyfx.org/) provides a Groovy binding, or DSL, for JavaFX 8.
There are later versions of JavaFX, but GroovyFX doesn't support them yet.

### Running the examples

Groovy code examples can be found in the [src/main/groovy](src/main/groovy) directory.

There are example scripts which:
* read from CSV files (multiple technologies)
* explore finding outliers
* look at displaying histograms showing the value spread for a particular feature (multiple technologies)
* illustrate simple regression against one feature (multiple technologies)
* illustrate multi-regression against multiple features (multiple technologies)
* look at displaying errors (multiple technologies)

You have several options for running the programs (see more details from the main [README](../../README.md#running-the-examples)):

* If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

* From the command line, invoke a script with gradlew using the appropriate run&lt;_ScriptName_&gt; task. To see the available tasks use (`./gradlew` on unix-like systems):\
 `gradlew :HousePricesGroovyFX:tasks --group="Application"`
* If the example has @Grab statements commented out at the top, you can cut and paste the examples into the groovyConsole
and uncomment the grab statements. Make sure to cut and paste any helper classes too if appropriate.

### Requirements

* GroovyFX examples require JDK 8 with JavaFX, e.g.&nbsp;Oracle JDK8 or Zulu JDK8 bundled with JavaFX.

### Troubleshooting

* Numerous examples create a Swing/JavaFX GUI, so aren't suitable for running in the normal way when using Gitpod.
