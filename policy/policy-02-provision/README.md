# Create a policy for provisioning 

This Sample provides a practical demonstration of using a **policy-driven approach** to enable secure file transfers between a **provider** and a **consumer**. It utilizes **MinIO**, an S3-compatible storage solution, to illustrate how assets can be managed with detailed access controls enforced by policies. The process covers everything from defining resources to negotiating contracts and transferring files, all while ensuring compliance with the defined policy constraints.

The main objective is to show how a file named `test-document` can be securely moved from a **provider-bucket** to a **consumer-bucket** in MinIO. This transfer is regulated by a policy that enforces location constraints, ensuring the entire process adheres to organizational or regulatory requirements.

# Provisioning and Policy Enforcement

Provisioning in this sample involves the secure and policy-regulated transfer of assets between a provider and a consumer using MinIO as an S3-compatible storage solution. The process ensures that resources are provisioned dynamically while adhering to defined policies that govern access controls, permissions, and constraints.

The policy used in this sample enforces a location constraint, ensuring that resources are only accessible in specific regions (e.g., `region = eu-central-1`). During provisioning, the policy framework evaluates constraints and applies necessary adjustments, such as dynamically updating the resource's region if it does not comply with the policy.



## Prerequisites

Ensure the following:
- Docker is installed: [Docker Installation](https://docs.docker.com/engine/install/).
- MinIO is configured as an S3-compatible storage emulator.
- The AWS CLI is installed and configured for interaction with MinIO: Ensure that the **AWS CLI** is installed and configured to interact with **MinIO**. Follow the [AWS CLI Integration Guide](https://min.io/docs/minio/linux/integrations/aws-cli-with-minio.html) for detailed steps.


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


### Step 10: Retrieve the Region of a Bucket

To retrieve the region of a bucket using the AWS CLI, use the following command:

```bash
aws --endpoint-url http://localhost:9000 s3api get-bucket-location --bucket <bucket-name>
```
### expected Output

```json
{
    "LocationConstraint": "eu-central-1"
}
```
## Stop docker container
Execute the following command in a terminal window to stop the docker container:
```bash
docker-compose -f policy/policy-02-provision/docker-compose.yml down
```

---
