# House price prediction with Apache Ignite

This project looks at scaling up the [HousePrices](../HousePrices/)
project using [Apache Ignite](https://ignite.apache.org/).

<img src="../../docs/images/cottage.png" height="250" alt="house"/>

## Ignite overview

[Apache Ignite](https://ignite.apache.org/) is a "distributed database" for high-performance computing with in-memory speed.

## Implementation overview

Linear regression is a common algorithm used for training a model which can then be used for prediction. It is described further in the
main [HousePrices](../HousePrices#linear-regression) project.
Ignite supports a machine learning library `ML` which includes a clustered regression implementation (we used `LSQRTrainer`).

### Running the examples

Groovy code examples can be found in the [src/main/groovy](src/main/groovy) directory.

You have several options for running the programs (see more details from the main [README](../../README.md#running-the-examples) in the root project):

* If you have opened the repo in IntelliJ (or your favourite IDE) you should be able to execute the examples directly in the IDE.

* From the command line, invoke the application using gradlew (use `./gradlew` on unix-like systems) with the run command.\
  `gradlew :HousePricesIgnite:run`

* If the example has @Grab statements commented out at the top, you can cut and paste the examples into the groovyConsole
and uncomment the grab statements. Make sure to cut and paste any helper classes too if appropriate.

### Requirements

It has been tested on JDK8, JDK11 and JDK17.
