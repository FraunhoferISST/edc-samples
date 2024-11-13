# Base Federated Catalog

The purpose of this example is to make preparations for implementing Federated Catalog (FC).
For that we'll set up a basic federated catalog that includes necessary dependencies for triggering the FC.

We will call it federated catalog base, and will use this for our future implementations to trigger FC. This sample demonstrates
a [build.gradle.kts](./federated-catalog-base/build.gradle.kts) file consisting of
only the dependencies necessary for FC and do not include any other functionalities. Required dependencies will be added
in the later samples based on their use cases.

### federated-catalog-base
### static-node-resolver
### connector

## Create a connector with contract offer
```shell
./gradlew transfer:transfer-00-prerequisites:connector:build
```

```shell
java -Dedc.keystore=transfer/transfer-00-prerequisites/resources/certs/cert.pfx -Dedc.keystore.password=123456 -Dedc.fs.config=transfer/transfer-00-prerequisites/resources/configuration/provider-configuration.properties -jar transfer/transfer-00-prerequisites/connector/build/libs/connector.jar
```
#### Create asset
```shell
curl -d @transfer/transfer-01-negotiation/resources/create-asset.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/assets \
  -s | jq
```

#### Create a Policy on the provider
```bash
curl -d @transfer/transfer-01-negotiation/resources/create-policy.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/policydefinitions \
  -s | jq
```

#### Create a contract definition on Provider
```bash
curl -d @transfer/transfer-01-negotiation/resources/create-contract-definition.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/contractdefinitions \
  -s | jq
```