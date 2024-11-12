# Standalone Federated Catalog with Node resolver

This sample demonstrates how we can implement a federated catalog which is embedded in a connector.
We will build one connector of such type and will call it `fc-connector`.
To keep things simple at the beginning, we will only have the most basic implementation of an embedded FC.
For this case, we will assume our connector has no node directory, and thus we will not be implementing any
TargetNodeDirectory resolver.


This sample will go through:

* Implementation of an embedded FC
* Building the embedded FC connector, `fc-connector`
* Running `fc-connector`

## Run the sample
### 1. Implementation the fc-connector

The build file x file in x directory contains all the necessary dependencies for a creating a connector,
along with the `fc-00-basic:federated-catalog-base` to trigger the FC.
```shell
dependencies {
    runtimeOnly(project(":fc:fc-00-basic:federated-catalog-base"))
    ...
}
```
### 2. Build the fc-connector

Execute this command in project root to build the fc-connector JAR file:

```bash
./gradlew fc:fc-03-resolve-node-directory:standalone-fc-with-node-resolver:build
```


### 3. Run the fc-connectors

To run the connector, execute the following command

```shell
java -Dedc.fs.config=fc/fc-02-standalone/standalone-catalog/config.properties -jar fc/fc-03-resolve-node-directory/standalone-fc-with-node-resolver/build/libs/standalone-fc-with-node-resolver.jar
```
You can also run the JAR file in debug mode using the following command,
```shell
java -Dedc.fs.config=fc/fc-01-embedded/fc-connector/config.properties -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006 -jar fc/fc-01-embedded/fc-connector/build/libs/fc-connector.jar
```
If the execution is successful, then the Catalog API of our connector will listen on the port a`19195`.

[//]: # (INFO 2024-11-10T19:36:02.150032714 Runtime 2ec743c7-b39c-45b9-9e13-e750cf89d102 ready)
If you observe the logs, you can see the following two recurring line,
```
DEBUG 2024-11-10T19:36:07.133919175 [ExecutionManager] Run pre-execution task
DEBUG 2024-11-10T19:36:07.137216546 [ExecutionManager] No WorkItems found, skipping execution
...
```
This means our FC crawler is running, and the crawler did not find any WorkItem
as we do not have any node directory.




## build jar
```shell
./gradlew clean fc:fc-02-standalone:catalog-node-resolver:build
```

## Resolver
#### run connector
```shell
java -Dedc.fs.config=fc/fc-01-standalone/catalog-node-resolver/connector1/config.properties -jar fc/fc-1/fc-connector/build/libs/connector3.jar
```
For debug:
```shell
java -Dedc.fs.config=fc/fc-1/fc-connector/connector1/config.properties -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006 -jar fc/fc-1/fc-connector/build/libs/connector3.jar
```
catalog-node-resolver

```shell
java -Dweb.http.catalog.path="/api/catalog" \
-Dweb.http.catalog.port=8181 \
-Dweb.http.path="/api" \
-Dweb.http.port=8080 \
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
-jar fc/fc-01-standalone/catalog-node-resolver/build/libs/catalog-node-resolver.jar
```