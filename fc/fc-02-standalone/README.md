# Standalone Federated Catalog

This sample demonstrates how we can implement a standalone federated catalog.


This sample will go through:

* Implementation of a standalone FC
* Building the standalone FC
* Running standalone FC

## Run the sample
### 1. Implementation 

Something on Implementation and config properties

### 2. Build 

Execute this command in project root to build the fc-connector JAR file:

```bash
./gradlew fc:fc-02-standalone:standalone-catalog:build
```


### 3. Run 

To run the connector, execute the following command

```shell
java -Dedc.fs.config=fc/fc-02-standalone/standalone-catalog/config.properties -jar fc/fc-02-standalone/standalone-catalog/build/libs/standalone-catalog.jar
```

If the execution is successful, then the Catalog API will listen on the port a`29195`.