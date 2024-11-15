# Standalone Federated Catalog with Node resolver



## Run the sample
### 1. Implementation 


### 2. Build 

Execute this command in project root to build the JAR file:

```bash
./gradlew fc:fc-03-static-node-directory:standalone-fc-with-node-resolver:build
```


### 3. Run 

To run the connector, execute the following command

```shell
java -Dedc.fs.config=fc/fc-02-standalone/standalone-fc/config.properties -jar fc/fc-03-static-node-directory/standalone-fc-with-node-resolver/build/libs/standalone-fc-with-node-resolver.jar
```

If the execution is successful, then the Catalog API will listen on the port `29195`.