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

# House price prediction with regression

Linear regression will enable us to find a "best fit" linear relationship between
some properties or features of interest.
Ordinary least squares finds such a relationship by minimising residual errors.
This example uses linear regression to explore predicting house prices from a number
of other features such as number of bedrooms, number of bathrooms, living space etc.
The examples illustrate several alternative dataframe libraries,
several CVS handling libraries and a number of visualization options.

![linear regression house prices](../../docs/images/houses.png)

Groovy code examples can be found in the [HousePrices](subprojects/HousePrices/src/main/groovy) subproject.
You have several options for running the programs:

* If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

* You can run the main examples online using a Jupyter/Beakerx notebook:
[![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/paulk-asert/groovy-data-science/master?filepath=subprojects%2FHousePrices%2Fsrc%2Fmain%2Fnotebook%2FHousePrices.ipynb)

* From the command line, invoke a script with gradlew using the appropriate run&lt;_ScriptName_&gt; task.
  (Hint: `gradlew :HousePrices:tasks --group="Script"` will show you available task names.)
* If the example has @Grab statements commented out at the top, you can cut and paste the examples into the groovyConsole
and uncomment the grab statements. Make sure to cut and paste any helper classes too if appropriate.

Alternatively, you can run the main examples online using a Jupyter/Beakerx notebook:

It can be potentially difficult to scale linear regression.
How do you minimise residual errors on data spread across different
threads/clusters/CPUs?
Some regression algorithm variants like stochastic gradient descent are amenable to scaling.
And some frameworks support such algorithms. The following subprojects highlight frameworks
with special support for scaling linear regression:

* The [HousePricesIgnite](subprojects/HousePricesIgnite/src/main/groovy) subproject which illustrates scaling up to a cluster using Apache Ignite.

* The [HousePricesSpark](subprojects/HousePricesSpark/src/main/groovy) subproject which illustrates scaling up to a cluster using Apache Spark.

If you find that your algorithm isn't directly amenable to scaling
you can often tweak it or apply it in some fashion to ensure certain
constraints hold. This can enable you to still scale up.
The following subprojects highlight tweaking linear regression
for scaling purposes:

* The [HousePricesBeam](subprojects/HousePricesBeam/src/main/groovy) subproject which illustrates scaling up to a cluster using Apache Beam.

* The [HousePricesGPars](subprojects/HousePricesGPars/src/main/groovy) subproject which illustrates scaling up concurrently using GPars.

__Requirements__:
* GroovyFX examples require JDK 8 with JavaFX, e.g. Oracle JDK8 or Zulu JDK8 bundled with JavaFX.
* Numerous examples create a Swing/JavaFX GUI, so aren't suitable for running in the normal way when using Gitpod.
* Some examples use Tablesaw Plot.ly integration which fires open a browser. These will give an error if run
  using Gitpod but will create a file in the `build` folder which you can then open by right-clicking
  in the Gitpod browser then "Open With -> Preview".
