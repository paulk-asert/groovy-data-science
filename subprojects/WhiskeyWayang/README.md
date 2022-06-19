# Whiskey cluster centroids with Apache Wayang

In an attempt to find the perfect single-malt Scotch whiskey,
the whiskies produced from [86 distilleries](https://www.niss.org/sites/default/files/ScotchWhisky01.txt) have been ranked
by expert tasters according to 12 criteria (Body, Sweetness, Malty, Smoky, Fruity, etc.).

![Clustering](../../docs/images/clustering_bottles.jpg)

While those rankings might prove interesting reading to some Whiskey advocates,
it is difficult to draw many conclusions from the raw data alone.
Clustering is a well-established area of statistical modelling where
data is grouped into clusters.
Members within a cluster should be similar to each other and
different from the members of other clusters.
Clustering is an unsupervised learning method.
The categories are not predetermined but instead represent natural groupings
which are found as part of the clustering process.

K-Means is the most common form of _centroid_ clustering.
The K represents the number of clusters to find.
This example uses K-Means centroids to look at grouping together
the Whiskey drinks in our case study.

Groovy code examples can be found in the [src/main/groovy](src/main/groovy) directory.

You have several options for running the programs (see more details from the main [README](../../README.md#running-the-examples)):

* If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

* You can run the Java stream backed example online using a Jupyter/Beakerx notebook (slightly different code since it uses Groovy 2.5.6 and JDK8):
[![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/paulk-asert/groovy-data-science/master?filepath=subprojects%2FWhiskeyWayang%2Fsrc%2Fmain%2Fnotebook%2FWhiskeyWayang.ipynb)
* From the command line, invoke a script with gradlew with the run command.\
  `gradlew :WhiskeyWayang:run`
* If the example has @Grab statements commented out at the top, you can cut and paste the examples into the groovyConsole
and uncomment the grab statements. Make sure to cut and paste any helper classes too if appropriate.


### Requirements

  This example has been tested on JDK8 and JDK11. The application nominates possible Java and Spark platforms for processing. 
  Apache Wayang automatically selects between these platforms.
  The application will succeed if the Java platform is selected when using JDK17.
  The application will fail if the Spark platform is selected as current Spark versions are not compatible with JDK17.
