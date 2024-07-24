## build jar
```shell
./gradlew clean fc:fc-connector:build
```


## Connector 1
#### run connector
```shell
java -Dedc.fs.config=fc/fc-connector/connector1/config.properties -jar fc/fc-connector/build/libs/connector3.jar
```
For debug:
```shell
java -Dedc.fs.config=fc/fc-connector/connector1/config.properties -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006 -jar fc/fc-connector/build/libs/connector3.jar
```
#### Create asset
```shell
curl -d @fc/fc-connector/connector1/resources/create-asset.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/assets \
  -s | jq
```

#### Create a Policy on the provider
```bash
curl -d @fc/fc-connector/connector1/resources/create-policy.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/policydefinitions \
  -s | jq
```

#### Create a contract definition on Provider
```bash
curl -d @fc/fc-connector/connector1/resources/create-contract-definition.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/contractdefinitions \
  -s | jq
```




## Connector 3
#### run connector
```shell
java -Dedc.fs.config=fc/fc-connector/connector3/config.properties -jar fc/fc-connector/build/libs/connector3.jar
```

#### Create asset
```shell
curl -d @fc/fc-connector/connector3/resources/create-asset.json \
  -H 'content-type: application/json' http://localhost:39193/management/v3/assets \
  -s | jq
```

#### Create a Policy on the provider
```bash
curl -d @fc/fc-connector/connector3/resources/create-policy.json \
  -H 'content-type: application/json' http://localhost:39193/management/v3/policydefinitions \
  -s | jq
```

#### Create a contract definition on Provider
```bash
curl -d @fc/fc-connector/connector3/resources/create-contract-definition.json \
  -H 'content-type: application/json' http://localhost:39193/management/v3/contractdefinitions \
  -s | jq
```





## Connector 2
#### run connector
```shell
java -Dedc.fs.config=fc/fc-connector/connector2/config.properties -jar fc/fc-connector/build/libs/connector3.jar
```
For debug:
```shell
java -Dedc.fs.config=fc/fc-connector/connector2/config.properties -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar fc/fc-connector/build/libs/connector3.jar
```

#### catalog request
```shell
curl -X POST "http://localhost:29195/catalog/v1alpha/catalog/query" \
    -H 'Content-Type: application/json' \
    -d @fc/fc-connector/connector2/empty-request-body.json -s | jq
```