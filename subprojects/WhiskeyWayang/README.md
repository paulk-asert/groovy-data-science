# Whiskey cluster centroids with Apache Wayang

This project looks at scaling up the [Whiskey](../Whiskey/)
project using [Apache Wayang](https://wayang.apache.org/).

![Clustering](../../docs/images/clustering_bottles.jpg)

## Wayang overview

[Apache Wayang](https://wayang.apache.org/) (incubating) is an API for big data cross-platform processing.
It provides an abstraction over other platforms like
[Apache Spark](https://spark.apache.org/) and
[Apache Flink](https://flink.apache.org/) as well as a default built-in stream-based "platform".
Some keys goals of Wayang are:
* a processing platform independent developer experience when writing applications,
* a processing platform independent approach to executing applications,
* and support for optimizing execution across processing platforms.

The code developers write is intended to be the same regardless of whether a light-weight
or highly-scalable processing platform may eventually be used to execute it.
Execution of the application is specified in a platform-agnostic logical plan.
Wayang transforms the logical plan into a set of physical operators
to be executed by one or more nominated underlying processing platforms.

## Implementation overview

K-Means is the most common form of _centroid_ clustering
and is described further in the main [Whiskey](../Whiskey#kmeans) project.
This example uses K-Means centroids to look at grouping together
the Whiskey drinks in our case study.
We have our own hand-crafted K-Means algorithm which will be executed
across the selected processing platform.

### Running the examples

Groovy code examples can be found in the [src/main/groovy](src/main/groovy) directory.

You have several options for running the programs (see more details from the main [README](../../README.md#running-the-examples) in the root project):

* If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

* You can run the Java stream backed example online using a Jupyter/Beakerx notebook (slightly different code since it uses Groovy 2.5.6 and JDK8):
[![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/paulk-asert/groovy-data-science/master?filepath=subprojects%2FWhiskeyWayang%2Fsrc%2Fmain%2Fnotebook%2FWhiskeyWayang.ipynb)
* From the command line, invoke the application using gradlew (use `./gradlew` on unix-like systems) with the run command.\
  `gradlew :WhiskeyWayang:run`
* If the example has @Grab statements commented out at the top, you can cut and paste the examples into the groovyConsole
and uncomment the grab statements. Make sure to cut and paste any helper classes too if appropriate.

### Requirements

This example has been tested on JDK8, JDK11, and JDK17. The application nominates possible Java and Spark platforms for processing. 
Apache Wayang automatically selects between these platforms and for this application will always select Java. Comment out the Java plugin to force execution on the Spark platform.
