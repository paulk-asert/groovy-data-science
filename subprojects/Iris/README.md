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

# Iris Classification

These examples look at classifying samples of Iris flowers based
on petal width, petal length, sepal width, sepal length.

![Iris Species](../../docs/images/IrisSpecies.png)

One of the approaches used will be to create a decision tree.

![Iris decision tree](../../docs/images/IrisDecisionTree.png)

There are three species: Iris-setosa, Iris-versicolor, Iris-virginica. 

![Iris Classes](../../docs/images/IrisClasses.png)

Groovy code examples can be found in the [src/main/groovy](src/main/groovy) directory.

## Running the examples

You have several options for running the programs (see more details from the main [README](../../README.md#running-the-examples)).

* If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

* From the command line, invoke a script with gradlew using the appropriate run&lt;_ScriptName_&gt; task.
  (Hint: `gradlew :Iris:tasks --group="Script"` will show you available task names.)

* If the example has @Grab statements commented out at the top, you can cut and paste the examples into the groovyConsole
  and uncomment the grab statements. Make sure to cut and paste any helper classes too if appropriate.

### Requirements

* Examples should run fine in JDK8 or JDK11.
* The JSAT JavaFX example requires a JDK with JavaFX included.

### Troubleshooting

* Numerous examples create a Swing/JavaFX GUI, so aren't suitable for running in the normal way when using Gitpod.
* Some examples use Tablesaw Plot.ly integration which fires open a browser. These will give an error if run
  using Gitpod but will create a file in the `build` folder which you may be able to preview (see earlier comments).
