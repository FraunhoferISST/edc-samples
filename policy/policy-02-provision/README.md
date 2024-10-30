# Create a policy for provisioning 

This Sample demonstrates a policy-driven approach to secure file transfers between a provider and consumer, utilizing MinIO as an S3-compatible storage solution. The setup provides a framework for managing assets with fine-grained access controls, enforced by policies. It showcases the steps required to establish assets, define and enforce policies, negotiate contracts, and transfer files according to specified policy constraints. This example will walk you through setting up MinIO, configuring connectors, and executing a complete file transfer flow with policy-regulated asset access.

In this sample, a file named `test-document` will be uploaded to the **provider-bucket** in MinIO. Once the file transfer is complete, the document will be moved to the **consumer-bucket** on the consumer’s side.


## Prerequisites

Ensure the following:
- Docker is installed: [Docker Installation](https://docs.docker.com/engine/install/).
- MinIO is configured as an S3-compatible storage emulator.

## Set Up MinIO

### Step 1: Start MinIO

Use `docker-compose` to start MinIO:

```bash
docker-compose -f policy/policy-02-provision/docker-compose.yml up -d
```

### Step 2: Create and Configure a MinIO Bucket

1. Go to [MinIO Console](http://localhost:9001).
2. Log in with the credentials in the `docker-compose.yml` file (line 12-13).
3. Navigate to **Buckets** and create a bucket named **provider-bucket** for storing assets.

## Build and Start the Connectors

Before making the first request, build and run the provider and consumer connectors for this sample.

### Build and Run the Consumer Connector

```shell
./gradlew policy:policy-02-provision:policy-provision-consumer:build

java -Dedc.fs.config=policy/policy-02-provision/policy-provision-consumer/config.properties -jar policy/policy-02-provision/policy-provision-consumer/build/libs/consumer.jar
# for windows
java -D"edc.fs.config"=policy/policy-02-provision/policy-provision-consumer/config.properties -jar policy/policy-02-provision/policy-provision-consumer/build/libs/consumer.jar
```

### Build and Run the Provider Connector

In another terminal, build and run the provider connector:

```shell
./gradlew policy:policy-02-provision:policy-provision-provider:build

java -Dedc.fs.config=policy/policy-02-provision/policy-provision-provider/config.properties -jar policy/policy-02-provision/policy-provision-provider/build/libs/provider.jar
# for windows
java -D"edc.fs.config"=policy/policy-02-provision/policy-provision-provider/config.properties -jar policy/policy-02-provision/policy-provision-provider/build/libs/provider.jar
```

## Define and Register Resources


### Step 1: Register the Asset on the Provider

Register an asset to make it available for transfer.

```bash
curl -d @policy/policy-02-provision/resources/create-asset.json \
  -H 'content-type: application/json' http://localhost:8182/management/v3/assets -s | jq
```

### Step 2: Define an Access Policy on the Provider

Create an access policy that will regulate file paths for this asset.

```bash
curl -d @policy/policy-02-provision/resources/create-policy.json \
  -H 'content-type: application/json' http://localhost:8182/management/v3/policydefinitions -s | jq
```

### Step 3: Create a Contract Definition on the Provider

Link the access policy to the asset by creating a contract definition, allowing discovery and negotiation.

```bash
curl -d @policy/policy-02-provision/resources/create-contract-definition.json \
  -H 'content-type: application/json' http://localhost:8182/management/v3/contractdefinitions -s | jq
```

### Step 4: Retrieve provider Contract Offers

The consumer retrieves the provider’s catalog to view available contract offers.

```bash
curl -X POST "http://localhost:9192/management/v3/catalog/request" \
    -H 'X-Api-Key: password' -H 'Content-Type: application/json' \
    -d @policy/policy-02-provision/resources/fetch-catalog.json -s | jq
```

Please replace the {{contract-offer-id}} placeholder in the [negotiate-contract.json](resources/negotiate-contract.json) file with the contract offer id you found in the catalog at the path dcat:dataset.odrl:hasPolicy.@id.



## Contract Negotiation and Transfer

### Step 5: Negotiate the Contract


```bash
curl -d @policy/policy-02-provision/resources/negotiate-contract.json \
  -H 'X-Api-Key: password' \
  -X POST -H 'Content-Type: application/json' \
  http://localhost:9192/management/v3/contractnegotiations -s | jq
```

### Step 6: Retrieve the Contract Agreement ID

To check the status and retrieve the contract agreement ID, use the negotiation ID from the previous step.

```bash
curl -X GET "http://localhost:9192/management/v3/contractnegotiations/{{contract-negotiation-id}}" \
    -H 'X-Api-Key: password' \
    --header 'Content-Type: application/json' -s | jq
```

Replace `{{contract-agreement-id}}` in [filetransfer.json](resources/filetransfer.json) with the retrieved agreement ID.

### Step 7: Initiate the File Transfer

With the agreement in place, initiate the file transfer to the **consumer-bucket** in MinIO.

```bash
curl -d @policy/policy-02-provision/resources/filetransfer.json \
  -H 'X-Api-Key: password' \
  -X POST -H 'Content-Type: application/json' \
  http://localhost:9192/management/v3/transferprocesses -s | jq
```

### Step 8: Verify Transfer Status

Use the transfer process ID to verify the file transfer status.

```bash
curl -H 'X-Api-Key: password' http://localhost:9192/management/v3/transferprocesses/<transfer-process-id> -s | jq
```

### Step 9: Verify the File in MinIO

Once the transfer is complete, check the **consumer-bucket** in MinIO for the transferred file. Access MinIO at [http://localhost:9000](http://localhost:9000) and log in with `admin` / `password`.

## Stop docker container
Execute the following command in a terminal window to stop the docker container:
```bash
docker-compose -f policy/policy-02-provision/docker-compose.yml down
```

---
