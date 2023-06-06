# Whiskey clustering with Apache Ignite

This project looks at scaling up the [Whiskey](../Whiskey/)
project using [Apache Ignite](https://ignite.apache.org/).

![Clustering](../../docs/images/clustering_bottles.jpg)

## Ignite overview

[Apache Ignite](https://ignite.apache.org/) is a "distributed database" for high-performance computing with in-memory speed.

## Implementation overview

K-Means is the most common form of _centroid_ clustering
and is described further in the main [Whiskey](../Whiskey#kmeans) project.
Ignite supports a machine learning library `ML` which includes a clustered K-Means implementation.

### Running the examples

Groovy code examples can be found in the [src/main/groovy](src/main/groovy) directory.

You have several options for running the programs (see more details from the main [README](../../README.md#running-the-examples) in the root project):

* You can run the main examples online using a Jupyter/Beakerx notebook:
  [![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/paulk-asert/groovy-data-science/master?filepath=subprojects%2FWhiskeyIgnite%2Fsrc%2Fmain%2Fnotebook%2FWhiskeyIgnite.ipynb)

* If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

* From the command line, invoke the application using gradlew (use `./gradlew` on unix-like systems) with the run command.\
  `gradlew :WhiskeyIgnite:run`

* If the example has @Grab statements commented out at the top, you can cut and paste the examples into the groovyConsole
and uncomment the grab statements. Make sure to cut and paste any helper classes too if appropriate.

### Requirements

It has been tested on JDK8, JDK11 and JDK17.
