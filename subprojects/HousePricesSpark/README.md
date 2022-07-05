# House price prediction with Apache Spark

This project looks at scaling up the [HousePrices](../HousePrices/)
project using [Apache Spark](https://spark.apache.org/).

<img src="../../docs/images/cottage.png" height="250" alt="house"/>

## Spark overview

[Apache Spark™](https://spark.apache.org/) is a multi-language engine for
executing data engineering, data science, and machine learning on single-node
machines or clusters.

## Implementation overview

Linear regression is a common algorithm used for training a model which can then be used for prediction. It is described further in the
main [HousePrices](../HousePrices#linear-regression) project.
Spark supports a machine learning library `MLlib` which includes a
scalable implementation, `LinearRegression`.

### Running the examples

Groovy code examples can be found in the [src/main/groovy](src/main/groovy) directory.

You have several options for running the programs (see more details from the main [README](../../README.md#running-the-examples) in the root project):

* If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

* From the command line, invoke the application using gradlew (use `./gradlew` on unix-like systems) with the run command.\
  `gradlew :HousePricesSpark:run`

* If the example has @Grab statements commented out at the top, you can cut and paste the examples into the groovyConsole
and uncomment the grab statements. Make sure to cut and paste any helper classes too if appropriate.

### Requirements

It has been tested on JDK8 and JDK11. The current Spark versions are not compatible with JDK17.
