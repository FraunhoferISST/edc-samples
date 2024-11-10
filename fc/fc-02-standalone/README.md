## build jar
```shell
./gradlew clean fc:fc-01-standalone:catalog-node-resolver:build
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