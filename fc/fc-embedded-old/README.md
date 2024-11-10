# Embedded Federated Catalog

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
./gradlew clean fc:fc-02-embedded:fc-connector:build
```

After the build end you should verify that the connector.jar is created in the directory
[/connector/build/libs/connector.jar](connector/build/libs/connector.jar)

We can use the same .jar file for both connectors. Note that the consumer and provider connectors differ in their configuration.

Inspect the different configuration files below:

* [provider-configuration.properties](resources/configuration/provider-configuration.properties)
* [consumer-configuration.properties](resources/configuration/consumer-configuration.properties)

### 3. Run the fc-connectors

To run the connector, execute the following command

```shell
java -Dedc.fs.config=fc/fc-02-embedded/fc-connector/connector1/config.properties -jar fc/fc-02-embedded/fc-connector/build/libs/connector3.jar
```
You can also run the JAR file in debug mode using the following command,
```shell
java -Dedc.fs.config=fc/fc-02-embedded/fc-connector/connector1/config.properties -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006 -jar fc/fc-02-embedded/fc-connector/build/libs/connector3.jar
```
Assuming you didn't change the ports in config files, the consumer will listen on the
ports `29191`, `29192` (management API) and `29292` (DSP API) and the provider will listen on the
ports `12181`, `19182` (management API) and `19282` (DSP API).

The connectors have been configured successfully and are ready to be used.
