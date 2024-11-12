# Federated Catalog Samples

The samples in this section focus on the topic of Federated Catalogs.

### Motivation
The Federated Catalog (FC) represents the aggregated catalogs of multiple participants in a dataspace. To achieve that, the FC employs a set of crawlers, that periodically scrape the dataspace requesting the catalog from each participant in a list of participants and consolidates them in a local cache.
Instead of querying each participant individually, keeping a locally cached version of every participant's catalog makes catalog queries more responsive and robust, and it can cause a reduction in network load.

The following samples shows how to
* implement, build and run different versions of FC e.g.
  * standalone,
  * embedded,
  * and hybrid.
* extend a custom TargetNodeDirectory -
  * from a static file containing all participants' DSP endpoints,
  * from a static file containing all participants' DIDs
  * from external API
* modify execution plan (later) -
  * Event-driven mechanisms
  * Manual trigger via API



## Samples

### [FC sample 00](./fc-00-basic/README.md): Base federated catalog
The purpose of this example is to make preparations for implementing Federated Catalog (FC).
We'll set up a basic federated catalog that includes necessary dependencies for triggering the FC.

### [FC sample 01](./fc-01-embedded/README.md): Implement a federated catalog embedded in a connector
This sample demonstrates how we can implement a federated catalog which is embedded in a connector.
To keep things simple, we will only have the most basic implementation of an embedded FC.
For this case, we will assume our connector has no node directory, and thus we will not be implementing any
TargetNodeDirectory resolver.

What this sample will show:
* How to implement (build dependencies and configuration), build, and run an embedded FC.
* Observe how the crawler runs in interval.

What this sample will NOT show:
* As we do not have any node directory or node resolver, we can not show querying or retrieving catalogs.

### [FC sample 02](./fc-02-standalone/README.md): Implement a standalone federated catalog

This sample demonstrates how we can implement a standalone federated catalog.
In this case also we assume our connector has no node directory, and thus we will not be implementing any TargetNodeDirectory resolver.

What this sample will show: (same as FC sample 01)
* How to implement (build dependencies and configuration), build, and run an embedded FC.
* Observe how the crawler runs in interval.

What this sample will NOT show: (same as FC sample 01)
* As we do not have any node directory or node resolver, we can not show querying or retrieving catalogs.

### [FC sample 03](./fc-03-resolve-node-directory/README.md): Extend Node Directory Resolver
In this sample you will learn how to customise a TargetNodeDirectory depending on different use cases.

#### [FC sample 03.01](./fc-03-resolve-node-directory/README.md): Resolve node directories from participants' DSP endpoints
We will have the following implementation,
* An extension of the TargetNodeDirectory that resolves the nodes from a static file containing the DSP endpoints of the participants.
* Implementation of FC that uses this extension, e.g.
  * A standalone FC that uses this TargetNodeDirectory extension and our previous fc-02-standalone.
  * An embedded FC that uses this TargetNodeDirectory extension and our previous fc-01-embedded.
* Query the catalog endpoint.


What this sample will show:
* As we are using a node resolver, we can show querying and retrieving catalogs. Although it will return an empty list as we do not have any connector with contract offers yet.


What this sample will NOT show:
* We are using a hard-coded file with DSP endpoints of the participants as our node directory and do not include any did resolver in this case. Which is not the case in real scenario.

Considerations:
* Include a connector with contract offer just for demonstration?

#### [FC sample 03.02](): Resolve node directories from participants' DIDs
(later)


### [FC sample 04](): Modify Execution Plan
(later)