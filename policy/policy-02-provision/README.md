# Create a Policy for Provisioning

This sample demonstrates a **policy-driven approach** to secure file transfers between a **provider** and a **consumer**. It utilizes **AWS S3 (simulated via LocalStack)** to manage assets with enforced access controls. The process covers defining resources, negotiating contracts, and transferring files while ensuring compliance with policy constraints.

The objective is to securely transfer a file named `test-document.txt` from a **provider-bucket** to a **consumer-bucket** in **LocalStack S3**. The transfer is regulated by a policy enforcing location constraints to ensure compliance with organizational or regulatory requirements.

# Provisioning and Policy Enforcement

Provisioning in this sample ensures the **secure and policy-regulated** transfer of assets between a provider and a consumer using **AWS S3 in LocalStack** by dynamically allocating resources and enforcing policies before any data transfer occurs. The process begins with **resource definition**, where the provider specifies attributes such as bucket names, access credentials, and regional constraints. Next, **policy enforcement** ensures compliance by evaluating constraints like permitted regions, automatically adjusting resources if necessary. During **contract negotiation**, the provider and consumer establish access rules and conditions before initiating the transfer. Once the contract is agreed upon, **transfer execution** provisions the required storage, validates permissions, and executes the file transfer.

## Key Provisioning Code

Below is a key snippet from the provisioning implementation:

```java
@Override
public CompletableFuture<StatusResult<ProvisionResponse>> provision(S3BucketResourceDefinition resourceDefinition, Policy policy) {
    var rq = S3ClientRequest.from(resourceDefinition.getRegionId(), resourceDefinition.getEndpointOverride());
    var s3AsyncClient = clientProvider.s3AsyncClient(rq);
    monitor.debug("Provisioning S3 bucket: " + resourceDefinition.getBucketName() +
            " in region: " + resourceDefinition.getRegionId());
    var request = CreateBucketRequest.builder()
            .bucket(resourceDefinition.getBucketName())
            .createBucketConfiguration(CreateBucketConfiguration.builder().build())
            .build();

    monitor.debug("S3Provisioner: create bucket " + resourceDefinition.getBucketName());
    return s3AsyncClient.createBucket(request)
            .thenApply(response -> provisionSucceeded(resourceDefinition));
}
```

The `provision` function automatically creates an S3 bucket in **LocalStack S3** while ensuring security policies are followed. It sets up an **S3 client**, logs the provisioning action, builds a request with the **bucket name**, and sends it. Once the bucket is successfully created, the system logs the result and confirms its availability.
## Prerequisites

Ensure you have the following installed:

- **Docker**: [Docker Installation Guide](https://docs.docker.com/engine/install/)
- **AWS CLI**: [AWS CLI Installation Guide](https://aws.amazon.com/cli/)
- **LocalStack**: A local AWS cloud service emulator

## Set Up LocalStack

### Step 1: Start LocalStack

Use `docker-compose` to start LocalStack:

```bash
docker-compose -f policy/policy-02-provision/resources/docker-compose.yml up -d
```

### Step 2: Verify LocalStack S3 Setup

Check if S3 is running correctly:

```bash
aws --endpoint-url=http://localhost:4566 s3 ls
```

## Build and Start the Connectors

First, we need to build and start both our connectors. Execute the following commands from the project root in two
separate terminal windows (one per connector):
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

Please replace the {{contract-offer-id}} placeholder in the [negotiate-contract.json](resources/negotiate-contract.json) with the contract offer id you found in the catalog at the path dcat:dataset.odrl:hasPolicy.@id.



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

With the agreement in place, initiate the file transfer to the **consumer-bucket** in LocalStack.

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

### Step 9: Verify the File in LocalStack S3

```bash
aws --endpoint-url=http://localhost:4566 s3 ls s3://<bucket-name>
```
### Expected Output
```json
{
    "Key": "test-document.txt"
}
```

### Step 10: Retrieve the Region of a Bucket

```bash
aws --endpoint-url http://localhost:4566 s3api get-bucket-location --bucket <bucket-name>
```
### Expected Output
```json
{
    "LocationConstraint": "eu-central-1"
}
```

### Step 11: List Objects in a Bucket

```bash
aws --endpoint-url=http://localhost:4566 s3api list-objects --bucket <bucket-name>
```
### Expected Output
```json
{
    "Contents": [
        {
            "Key": "test-document.txt"
        }
    ]
}
```

## Stop LocalStack

To stop the LocalStack container:

```bash
docker-compose -f policy/policy-02-provision/resources/docker-compose.yml down
```

---
