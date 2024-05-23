# Whiskey clustering with Apache Flink®

This project looks at scaling up the [Whiskey](../Whiskey/)
project using [Apache Flink](https://flink.apache.org/).

![Clustering](../../docs/images/clustering_bottles.jpg)

## Flink overview

[Apache Flink](https://flink.apache.org/) is a framework and distributed processing engine for stateful computations over unbounded and bounded data streams.

## Implementation overview

K-Means is the most common form of _centroid_ clustering
and is described further in the main [Whiskey](../Whiskey#kmeans) project.
Flink supports a machine learning library `ML` which includes a K-Means implementation.

### Running the examples

Groovy code examples can be found in the [src/main/groovy](src/main/groovy) directory.

You have several options for running the programs (see more details from the main [README](../../README.md#running-the-examples) in the root project):

* If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

* From the command line, invoke the application using gradlew (use `./gradlew` on unix-like systems) with the run command.\
  `gradlew :WhiskeyFlink:run`

### Requirements

It has been tested on JDK8, JDK11 and JDK17.
