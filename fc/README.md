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

### [FC sample 01](./fc-01-embedded/README.md): Implement a federated catalog embedded in a connector


### [FC sample 02](./fc-02-standalone/README.md): Implement a standalone federated catalog

### [FC sample 03](./fc-03-resolve-node-directory/README.md): Extend TargetNodeDirectory
In this sample you will learn how to customise a TargetNodeDirectory depending on different use cases.

#### [FC sample 03.01](./fc-03-resolve-node-directory/README.md): Resolve node directories from participants' DSP endpoints

#### [FC sample 03.02](): Resolve node directories from participants' DIDs



### [FC sample 04](): Modify Execution Plan
(later)