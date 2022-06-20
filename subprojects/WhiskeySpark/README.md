# Whiskey clustering with Apache Spark

This project looks at scaling up the [Whiskey](../Whiskey/)
project using [Apache Spark](https://spark.apache.org/).

![Clustering](../../docs/images/clustering_bottles.jpg)

## Spark overview

[Apache Spark™](https://spark.apache.org/) is a multi-language engine for
executing data engineering, data science, and machine learning on single-node
machines or clusters.

## Implementation overview

K-Means is the most common form of _centroid_ clustering
and is described further in the main [Whiskey](../Whiskey#kmeans) project.
Spark supports a machine learning library `MLlib` which includes a scalable K-Means implementation.
The `MLlib` K-Means implementation a parallelized variant of the [k-means++](http://en.wikipedia.org/wiki/K-means%2B%2B) method
called [kmeans||](http://theory.stanford.edu/~sergei/papers/vldb12-kmpar.pdf).

### Running the examples

Groovy code examples can be found in the [src/main/groovy](src/main/groovy) directory.

You have several options for running the programs (see more details from the main [README](../../README.md#running-the-examples) in the root project):

* If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

* From the command line, invoke the application using gradlew (use `./gradlew` on unix-like systems) with the run command.\
  `gradlew :WhiskeySpark:run`

* If the example has @Grab statements commented out at the top, you can cut and paste the examples into the groovyConsole
and uncomment the grab statements. Make sure to cut and paste any helper classes too if appropriate.

### Requirements

It has been tested on JDK8 and JDK11. The current Spark versions are not compatible with JDK17.
