# `iris` - Deep Learning in Groovy with Deep Netts and GraalVM

Inspired by https://github.com/wololock/gttp

## Preparation

Steps shown for unix-like systems.
The assumption is that you have cloned this repo (or copied the files into a similar layout).

### Install Groovy

Steps if you don't have Groovy 4.0.3 installed and want to do so via SDKMAN on unix-like machines.
Alternatively, install by your preferred method.

```
$ sdk install groovy 4.0.3

$ sdk use groovy 4.0.3
```

### Install GraalVM 22.1 (JDK 17)

Steps if you don't have GraalVM 22.1 installed and want to do so via SDKMAN on unix-like machines.
Alternatively, install by your preferred method.

```
$ sdk install java 22.1.0.r17-grl

$ sdk use java 22.1.0.r17-grl

$ gu install native-image
```

### Prepare dependencies

We'll be typing in a few commands manually later.
It will be easier if all of our dependencies are in one directory.

```
$ ./gradlew IrisGraalVM:copyDependenciesToLib

$ cd subprojects/IrisGraalVM

$ ls build/lib
deepnetts-core-1.13.2.jar  log4j-api-2.17.2.jar       slf4j-api-1.7.36.jar     visrec-api-1.0.5.jar
groovy-4.0.3.jar           log4j-to-slf4j-2.17.2.jar  slf4j-simple-1.7.36.jar
```

Currently, if you use `deepnetts` default `log4j-core` dependency you will see errors
when trying to build the native image. It's a known issue being looked at.
We used `log4j-to-slf4j` (it needs to be paired with `slf4j-simple` or `logback-classic`)
to side-step the issue.

## Build our `iris` application

The remaining steps assume we are in the IrisGraalVM subdirectory (which is where you will be if you have followed the above steps).

### Compile `iris`

```
$ groovyc -cp "build/lib/*" --compile-static iris.groovy
```

### Run `iris` with `native-image-agent`

```
$ java -agentlib:native-image-agent=config-output-dir=conf/ -cp ".:build/lib/*" iris
```

### Build native image

The next step could take a minute or two.

```
$ native-image --report-unsupported-elements-at-runtime \
  --initialize-at-run-time=groovy.grape.GrapeIvy,deepnetts.net.weights.RandomWeights \
  --initialize-at-build-time \
  --no-fallback \
  -H:ConfigurationFileDirectories=conf/ \
  -cp ".:build/lib/*" \
  -Dorg.slf4j.simpleLogger.defaultLogLevel=WARN \
  iris
```

The `-Dorg.slf4j.simpleLogger.defaultLogLevel=WARN` part bakes that system property into
the native executable. It will quieten the somewhat verbose logging during the model building
phase. Leave it out if you want normal logging.

### Run `iris` as a standalone executable file

```
$ time ./iris
CLASSIFIER EVALUATION METRICS
Accuracy: 1.0 (How often is classifier correct in total)
Precision: 1.0 (How often is classifier correct when it gives positive prediction)
F1Score: 1.0 (Harmonic average (balance) of precision and recall)
Recall: 1.0 (When it is actually positive class, how often does it give positive prediction)

CONFUSION MATRIX
                          none    Iris-setosaIris-versicolor Iris-virginica
           none              0              0              0              0
    Iris-setosa              0             14              0              0
Iris-versicolor              0              0             19              0
 Iris-virginica              0              0              0             12


real    0m0.131s
user    0m0.096s
sys     0m0.029s
```
