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

# House price data preparation with Apache Camel

This project looks at scaling up the CSV processing part of
the [HousePrices](../HousePrices/) project using
[Apache Camel](https://camel.apache.org/).

<img src="../../docs/images/cottage.png" height="250" alt="house"/>

## Camel overview

[Apache Camel](https://camel.apache.org/) is an Open Source integration framework that empowers you to quickly and easily integrate various systems consuming or producing data.
When collecting data such as our house prices, if the information was from
multiple sources or required several steps of processing, integration technologies
like Apache Camel might prove useful.

## Implementation overview

The diagram below shows the Camel architecture. It is centered around the Camel _context_.

<img src="https://camel.apache.org/manual/_images/images/camel-architecture.png" height="300" />

_Routes_ define how messages travel between endpoints.
We'll define our route in <span style="color:green"><b>Groovy</b></span> using Camel's Java DSL.

_Filters_ decide based on a predicate value whether delivery of
a particular message should continue. We'll use a <span style="color:green"><b>Groovy</b></span> predicate
with our filter.

_Processors_ transform or manipulate messages.
We'll define a <span style="color:green"><b>Groovy</b></span> processor that prints some message information to stdout.

### Running the examples

Groovy code examples can be found in the [src/main/groovy](src/main/groovy) directory.

You have several options for running the programs (see more details from the main [README](../../README.md#running-the-examples) in the root project):

* If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

* From the command line, invoke the application using gradlew (use `./gradlew` on unix-like systems) with the run command.\
  `gradlew :HousePricesCamel:run`

* If the example has @Grab statements commented out at the top, you can cut and paste the examples into the groovyConsole
  and uncomment the grab statements. Make sure to cut and paste any helper classes too if appropriate.

### Requirements

It has been tested on JDK8, JDK11 and JDK17.
When used on JDK8, the Camel LTS version for JDK8 is used,
otherwise a more recent version is used.
