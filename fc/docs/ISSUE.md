# Feature Request
Create sample that guides developers through the usage of federated catalog extensions.
This should include the demonstration of,
* implementation of connectors with federated catalog extensions,
* how to request catalogs using federated catalog extensions,
* why it is feasible compared to manual catalog request.


## Which Areas Would Be Affected?

A new `federated_catalog` module will be added int the [Samples](https://github.com/eclipse-edc/Samples) project.

## Why Is the Feature Desired?

This sample aims to help developers understand and use different functionalities of federated catalog.
The main component of federated catalog are,
* Target Catalog Node (TCN): Nodes, typically connectors that host their catalogs.
* Target Catalog Node Directory: A directory that lists all the TCNs, specifically the endpoints of these TCN (connector) catalogs.
* Federated Catalog Crawler (FCC): A periodic crawler that contacts the TCNs to collect their catalogs. It crawls the endpoints listed in TCN directory.

APIs,
* Catalog Query: a request coming from a client (other connectors) to get the entire catalog.
* Catalog Update Request: a DSP request issued by the crawler to other TCNs.


This sample presents how to include TCN and FCC in a connector with federated catalog extensions, how to implement TCN directory, and requesting catalogs through Catalog Query API.



## Solution Proposal

### Describe previous approach of requesting catalogs
The sample, with help of [transfer](https://github.com/eclipse-edc/Samples/tree/main/transfer) samples first describes how connectors without federated catalogs can be implemented and how we can request catalogs individually from each connector using the management endpoint.

![scen1](https://github.com/user-attachments/assets/3a7ba3bf-4763-4cb5-979d-624f63609a6a)


### Point out problems with traditional approach
It then points out the impracticality of requesting each connector individually. In real scenario a Dataspace may contain numerous connectors and contacting each of them separately each time to get updates on their catalog offers is infeasible. As it the figure above, for 3 connectors in the Dataspace requires each connector client to make 2 separately. In case of N connectors the clients (e.g. connector datadashboard UI) have to make N-1 requests each time.

### Describe federated catalog's workflow & advantages
The sample will then describe how federated catalog helps in this case. In federated catalog, we implement a Target Catalog Node Directory that can provide us with a list of Target Catalog Nodes(connectors) and their endpoints to crawl. This node directory component can be implemented in several ways, for example as a centralized registry service which stores information of all the connectors in a Dataspace and handles related tasks. However, in this sample we will implement the directory simply using a static json file which lists the TCNs. A crawler is employed that gets this TCN list from the directory and periodically crawls the listed endpoints and stores the collected catalog information in an internal cache.

![sce2](https://github.com/user-attachments/assets/5057cb7f-76ee-4193-9c39-3e3449a416a2)

Therefore, this catalog cache will always have updated information of the offers. The connector client can then use the Catalog Query API which exposes the internal cache.

![sce3](https://github.com/user-attachments/assets/35db59ae-9458-41f1-aa85-8aacceb2bd4a)


Thus, instead of querying each connector individually, clients can use this API to get a combined set of catalogs.
