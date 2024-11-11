## build jar
```shell
./gradlew clean fc:test-resources:regular-connector:build
```


## Regular Connector
#### run connector
```shell
java -Dedc.fs.config=fc/test-resources/regular-connector/config.properties -jar fc/test-resources/regular-connector/build/libs/rg-connector.jar
```
For debug:
```shell
java -Dedc.fs.config=fc/test-resources/regular-connector/config.properties -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006 -jar fc/test-resources/regular-connector/build/libs/rg-connector.jar
```

#### Create asset
```shell
curl -d @fc/test-resources/regular-connector/resources/create-asset.json \
  -H 'content-type: application/json' http://localhost:39193/management/v3/assets \
  -s | jq
```

#### Create a Policy on the provider
```bash
curl -d @fc/test-resources/regular-connector/resources/create-policy.json \
  -H 'content-type: application/json' http://localhost:39193/management/v3/policydefinitions \
  -s | jq
```

#### Create a contract definition on Provider
```bash
curl -d @fc/test-resources/regular-connector/resources/create-contract-definition.json \
  -H 'content-type: application/json' http://localhost:39193/management/v3/contractdefinitions \
  -s | jq
```