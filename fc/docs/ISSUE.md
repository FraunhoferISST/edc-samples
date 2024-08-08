## Feature Request
Create sample that guides developers through the usage of Federated Catalog (FC) extension.
This should include the demonstration of,
* implementation of node directory and FC cache,
* creation of connectors with federated catalog extensions,
* how to request catalogs using federated catalog extensions,
* why it is feasible compared to manual catalog request.


## Which Areas Would Be Affected?

A new `federated_catalog` module will be added int the [Samples](https://github.com/eclipse-edc/Samples) project.

## Why Is the Feature Desired?

It will demonstrate how the Federated Catalog (FC) extension can be used to maintain an up-to-date local cache of aggregated catalogs from multiple participants in a dataspace.





## Solution Proposal

* Describe previous approach of requesting catalogs and point out problems with the approach.

* Implement node directory, using a static json file, containing a list of all the TCNs. All the connectors use this single json file saved in the project directory.

* Implement an in-memory cache to store catalogs or FC cache.

* Create three connectors with FC extension, with at least two providing contract offers, to represent a dataspace featuring multiple participants.

* Demonstrate how to request catalogs using Catalog Query API

