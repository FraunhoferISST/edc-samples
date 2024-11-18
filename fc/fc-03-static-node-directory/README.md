# Catalog Node Resolver - Static Node Directory
The Federated Catalog requires a list of Target Catalog Nodes (TCN), which are essentially the participant connectors in the dataspace.
The catalog crawler then crawls the DSP endpoints of these listed nodes, and stores the consolidated set of catalogs in a Fderated Catalog Cache (FCC).


This list of Target Nodes is provided by the TargetNodeDirectory.
The TargetNodeDirectory serves as a 'phone book', maintaining specific information about the dataspace participants. It accepts an initial list of participants (e.g. list of participants' IDs), and resolves this input to a list of TargetNodes which is readable by the FC crawler.

The initial participant list may vary in its source and format depending on specific use cases. To accommodate these variations, different implementations of the TargetNodeDirectory can be created to customize the resolution process of Target Nodes from the provided participant list. In this sample, we will build a Catalog Node Resolver that reads the participants' data from a static file and resolves it into TargetNodes.


The code in this sample has been organized into several Java modules:

- `catalog-node-resolver`: contains the `TargetNodeDirectory` implementation which will list of Target Catalog Nodes.
- `embedded|standalone-fc-with-node-resolver`: the embedded/ standalone-fc that will be using the `catalog-node-resolver`.




## Implement the Catalog Node Resolver

### Participant file
The TargetNodeDirectory takes in a participant list and converts it to a list of TargetNodes. The TargetNode of a participant contains certain information about the participant along with its DSP endpoint, so the crawler knows which endpoints to crawl.

> In the simplest of cases, this participant list can be stored in a static file, but more complex implementations such as a centralized participant registry can also be implemented.

In this sample, our participant file is a static JSON that stores the TargetNode properties of our participants.
As we will be using the `participant-connector` from [fc-00], this [satic file]()contains the TargetNode properties of the `participant-connector` including its DSP endpoint.

### Target Node Resolver

#### TargetNodeResolver
The [TargetNodeResolver]() implements TargetNodeDirectory and overrides its `getAll()` method. In our implementation, this method maps the array of Json objects read from the [static file]() to a list of TargetNodes.

```java
public class CatalogNodeDirectory implements TargetNodeDirectory {
    //...
    @Override
    public List<TargetNode> getAll() {
        try {
            return objectMapper.readValue(participantListFile, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //...
}
```
The ExecutionManager invokes this method during the preparation phase of a crawler run, to obtain the list of TargetNodes. The crawler uses this list to query the DSP endpoints of the participants and collects the offered catalogs. The aggregated catalogs is stored in a Fderated Catalog Cache (FCC). In this example we are using the default implementation of a FCC, [InMemoryFederatedCatalogCache](https://github.com/eclipse-edc/FederatedCatalog/blob/main/core/federated-catalog-core/src/main/java/org/eclipse/edc/catalog/store/InMemoryFederatedCatalogCache.java).



## Run Federated Catalog with Node Resolver

Previously, we discussed the implementation of standalone and embedded FCs. In this example, we introduce two separate modules,`standalone-fc-with-node-resolver` and `embedded-fc-with-node-resolver`, which demonstrate the implementation of each type of federated catalogs that uses the TargetNodeResolver.

### Run standalone-fc with Node Resolver

#### Build the standalone-fc JAR
Execute this command in project root:

```bash
./gradlew fc:fc-03-static-node-directory:standalone-fc-with-node-resolver:build
```


#### Run the standalone-fc

To run the federated catalog, execute the following command

```shell
java -Dedc.fs.config=fc/fc-02-standalone/standalone-fc/config.properties -jar fc/fc-03-static-node-directory/standalone-fc-with-node-resolver/build/libs/standalone-fc-with-node-resolver.jar
```

If the execution is successful, then the Catalog API of our standalone FC will listen on port `29195`.

#### Test catalog query API
Before requesting the catalog API, make sure the `partcipant-connector` that we have set up in the
[fc-00-basic](../../fc/fc-00-basic) is running, and it has a contract offer.

To get the combined set of catalogs, use the following request:

```http request
curl -d @fc/fc-01-embedded/resources/empty-query.json \
  -H 'content-type: application/json' http://localhost:29195/api/catalog/v1alpha/catalog/query \
  -s | jq
```


### Run embedded-FC with Node Resolver

#### Build the standalone-fc JAR
Execute this command in project root:

```bash
./gradlew fc:fc-03-static-node-directory:standalone-fc-with-node-resolver:build
```


#### Run the standalone-fc

To run the federated catalog, execute the following command

```shell
java -Dedc.fs.config=fc/fc-02-standalone/standalone-fc/config.properties -jar fc/fc-03-static-node-directory/standalone-fc-with-node-resolver/build/libs/standalone-fc-with-node-resolver.jar
```

If the execution is successful, then the Catalog API of our standalone FC will listen on port `29195`.

#### Test catalog query API
Before requesting the catalog API, make sure the `partcipant-connector` that we have set up in the
[fc-00-basic](../../fc/fc-00-basic) is running, and it has a contract offer.

To get the combined set of catalogs, use the following request:

```http request
curl -d @fc/fc-01-embedded/resources/empty-query.json \
  -H 'content-type: application/json' http://localhost:29195/api/catalog/v1alpha/catalog/query \
  -s | jq
```
