# Data Science with Groovy

Groovy is a powerful multi-paradigm programming language for the JVM that offers a wealth of features that make it ideal for many data science and big data scenarios.

* Groovy has a dynamic nature like Python, which means that it is very powerful, easy to learn, and productive. The language gets out of the way and lets data scientists write their algorithms naturally.

* Groovy has a static nature like Java and Kotlin, which makes it fast when needed. Its close alignment with Java means that you can often just cut-and-paste the Java examples from various big data solutions and they’ll work just fine in Groovy.

* Groovy has first-class functional support, meaning that it offers features and allows solutions similar to Scala. Functional and stream processing with immutable data structures can offer many advantages when working in parallel processing or clustered environments.

This repo has examples of using Groovy with various Data Science libraries.

Math/Data Science libraries covered include:<br>
[Weka](https://www.cs.waikato.ac.nz/ml/weka/),
[Smile](http://haifengl.github.io/),
[Apache Commons Math](https://commons.apache.org/proper/commons-math/),
[Jupyter/Beakerx](http://beakerx.com/) notebooks,
[Tablesaw](https://tablesaw.tech/),
[Deep Learning4J](https://deeplearning4j.org/).

Libraries for scaling/concurrency include:<br>
[Apache Spark](https://spark.apache.org/),
[Apache Ignite](https://ignite.apache.org/),
[Apache MXNet](https://mxnet.apache.org/),
[GPars](http://gpars.org/),
[Apache Beam](https://beam.apache.org/).

### House price prediction with regression

Linear regression will enable us to find a "best fit" linear relationship between some properties or features of interest.
Ordinary least squares finds such a relationship by minimising residual errors.

Groovy code examples can be found in the [HousePrices](subprojects/HousePrices/src/main/groovy) subproject.
If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

Alternatively, you can run the main examples online using a Jupyter/Beakerx notebook:
[![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/paulk-asert/groovy-data-science/master?filepath=subprojects%2FHousePrices%2Fsrc%2Fmain%2Fnotebook%2FHousePrices.ipynb)

The complete repo has additional examples using alternative dataframe, CVS handling or visualization options. See also:

* The [HousePricesBeam](subprojects/HousePricesBeam/src/main/groovy) subproject which illustrates scaling up to a cluster using Apache Beam.

* The [HousePricesIgnite](subprojects/HousePricesIgnite/src/main/groovy) subproject which illustrates scaling up to a cluster using Apache Ignite.

* The [HousePricesSpark](subprojects/HousePricesSpark/src/main/groovy) subproject which illustrates scaling up to a cluster using Apache Spark.

* The [HousePricesGPars](subprojects/HousePricesGPars/src/main/groovy) subproject which illustrates scaling up concurrently using GPars.
In particular, this example illustrates how to tweak algorithms to improve how well they will work concurrently.
Standard least squares linear regression isn't ideal for parallel training but can be made to work reasonably well with some assumptions.
Other approaches like stochastic gradient descent should also be considered.

### Whiskey clustering with K-means

K-means is the most common form of “centroid” clustering.
Unlike classification, clustering is an unsupervised learning method.
The categories are not predetermined but instead represent natural groupings
which are found as part of the clustering process.
Members of each cluster should be similar to each other and
different from the members of the other clusters.
The K represents the number of clusters to find.

Groovy code examples can be found in the [Whiskey](subprojects/Whiskey/src/main/groovy) subproject.
If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

Alternatively, you can run the main examples online using a Jupyter/Beakerx notebook:
[![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/paulk-asert/groovy-data-science/master?filepath=subprojects%2FWhiskey%2Fsrc%2Fmain%2Fnotebook%2FWhiskey.ipynb)

The complete repo has additional examples using alternative clustering algorithms or visualization options. See also:

* The [WhiskeyIgnite](subprojects/WhiskeyIgnite/src/main/groovy) subproject which illustrates scaling up to a cluster using Apache Ignite.

* The [WhiskeySpark](subprojects/WhiskeySpark/src/main/groovy) subproject which illustrates scaling up to a cluster using Apache Spark.

### Cryptarithmetic puzzle with constraint programming

While not often spoken about as a classic data science technique,
constraint programming can be a very useful tool in numerous scenarios.

Groovy code examples can be found in the [SendMoreMoney](subprojects/SendMoreMoney/src/main/groovy) subproject.
If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

Alternatively, you can run the examples online using a Jupyter/Beakerx notebook:
[![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/paulk-asert/groovy-data-science/master?filepath=subprojects%2FSendMoreMoney%2Fsrc%2Fmain%2Fnotebook%2FSendMoreMoney.ipynb)

### Natural language processing

Groovy code examples can be found in the [LanguageDetect](subprojects/LanguageDetect/src/main/groovy) subproject.
If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

Alternatively, you can run the examples online using a Jupyter/Beakerx notebook:
[![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/paulk-asert/groovy-data-science/master?filepath=subprojects%2FLanguageDetect%2Fsrc%2Fmain%2Fnotebook%2FLanguageDetect.ipynb)

### Diet optimization

Groovy code examples can be found in the [Diet](subprojects/Diet/src/main/groovy) subproject.
If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

Alternatively, you can run the examples online using a Jupyter/Beakerx notebook:
[![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/paulk-asert/groovy-data-science/master?filepath=subprojects%2FDiet%2Fsrc%2Fmain%2Fnotebook%2FDiet.ipynb)
