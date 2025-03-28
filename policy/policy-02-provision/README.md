# Policy enforcement during provisioning

In this sample we'll learn how to apply policy enforcement during the provisioning phase of a transfer, to modify
a `ResourceDefinition` before the resource is actually provisioned. We can utilize this to ensure the resource is
provisioned in a way that it fulfils a given policy. This sample will focus on the policy enforcement during
provisioning and expects that you are generally familiar with the
[provisioning process](https://eclipse-edc.github.io/documentation/for-adopters/control-plane/#transfer-process-states).

The provider's policy will contain a location constraint with value `eu` (like the one we used in the first policy
sample). In the previous sample, we enforced this constraint by evaluating the location where the consumer is based.
In this sample, we want the data to be transferred into a storage located in Europe and will enforce this during the
consumer-provisioning process.

We'll use the
[provision-aws-s3 module](https://github.com/eclipse-edc/Technology-Aws/tree/main/extensions/control-plane/provision/provision-aws-s3),
which will provision an S3 bucket for the consumer before the transfer starts. The consumer will define `us-east-1` as
the desired AWS region of the bucket. We will create and register a `PolicyFunction`, that will modify the
`ResourceDefinition` for the consumer bucket and change the AWS region to `eu-central-1`, so that the bucket will then
be created in Europe. The sample consists of the following modules:

- `policy-provision-provider`: contains the build file and configuration for the provider
- `policy-provision-consumer`: contains the build file and configuration for the consumer, as well as the code for our policy function

Additionally, since we do not actually want to use AWS for this sample, a `docker-compose.yml` is located in the sample,
which we will use to start [LocalStack](https://www.localstack.cloud/), which provides AWS-compatible APIs.

## Creating the policy functions extensions

### Rule bindings

As shown in the previous sample, we again need to create rule bindings to bind both the action and the constraint we
will use in our policy to a scope for evaluation. This time, we choose the `MANIFEST_VERIFICATION_SCOPE`, which is
used during the provisioning phase of a transfer. 

### The policy function

Just like in the previous sample, we again need to create and register a policy function, which can evaluate our
constraint. The main difference now is, that due to using a different scope, we also use a different type of
`PolicyContext`, namely the `ProvisionManifestVerifyPolicyContext`.

```java
@Override
public boolean evaluate(Operator operator, Object rightValue, Permission rule, ProvisionManifestVerifyPolicyContext context) {

}
```

This context provides us with a `ResourceManifestContext`, which gives access to all `ResourceDefinitions` that have
been generated for the provisioning process by type. Thus, we can access the definitions of all
resources that will be provisioned, and a policy function using the `ProvisionManifestVerifyPolicyContext` is able
to modify these definitions before the corresponding resources are created.

This is also exactly what we will do in our example: we obtain all `ResourceDefinitions` of type
`S3BucketResourceDefinition` from the context, check whether the region the S3 buckets will be provisioned in is a
European region, and, if not, set the region to `eu-central-1`. Lastly, we update the definitions in the context.

```java
var manifestContext = context.resourceManifestContext();
var updatedDefinitions = manifestContext.getDefinitions(S3BucketResourceDefinition.class)
        .stream()
        .map(definition -> {
            if (!definition.getRegionId().startsWith(rightValueString)) {
                monitor.warning(format("Region does not start with '%s'. Setting to default: %s.", rightValueString, DEFAULT_REGION));
                return S3BucketResourceDefinition.Builder.newInstance()
                        .id(definition.getId())
                        .transferProcessId(definition.getTransferProcessId())
                        .regionId(DEFAULT_REGION)
                        .bucketName(definition.getBucketName())
                        .endpointOverride(definition.getEndpointOverride())
                        .build();
            }
            return definition;
        })
        .toList();

manifestContext.replaceDefinitions(S3BucketResourceDefinition.class, updatedDefinitions);
return true;
```

When the actual provisioning takes place afterward, it will be done on basis of the `ResourceDefinitions` returned
from the `ProvisionManifestVerifyPolicyContext`, i.e. in our example every S3 bucket will be created in a European
AWS region.

> **Note**: since this is just an example implementation for showcasing policy enforcement during provisioning, the
implementation expects the policy constraint operator to be `EQ`. Any other operator is not regarded here. If you
want to implement a similar function for a use-case, be sure to evaluate the actual operator and make modifications to
the `ResourceDefinitions` depending on this.

## Runtimes and configuration

The runtimes used for this sample are similar to the ones from the transfer samples, as this time, we are going to
run an actual data transfer. Since we are using S3 buckets in this transfer, the S3 data plane has been added.
Additionally, the S3 provisioning extension and our policy functions extension have been added to the consumer runtime.
For a complete list of modules, check out the respective build files of
[provider](./policy-provision-provider/build.gradle.kts) and [consumer](./policy-provision-consumer/build.gradle.kts).
Accordingly, you can find the configuration files in the respective directories.

## Run the sample

### Prerequisites

Ensure you have the following installed:

- [Docker](https://docs.docker.com/engine/install/)
- [Docker compose](https://docs.docker.com/compose/install/)
- [AWS CLI ](https://aws.amazon.com/cli/)

### Start LocalStack

To start LocalStack, run the following command:

```bash
docker compose -f policy/policy-02-provision/docker-compose.yml up
```

You can verify that LocalStack is started successfully using the AWS CLI: 

```bash
aws --endpoint-url=http://localhost:4566 s3 ls
```

### Build and run the connectors

First, we need to build and start both our connectors. Execute the following commands from the project root in two
separate terminal windows (one per connector):

#### Provider

```bash
./gradlew policy:policy-02-provision:policy-provision-provider:build
```
```bash
java -Dedc.fs.config=policy/policy-02-provision/policy-provision-provider/config.properties -jar policy/policy-02-provision/policy-provision-provider/build/libs/provider.jar --log-level=DEBUG
```

#### Consumer

```bash
./gradlew policy:policy-02-provision:policy-provision-consumer:build
```
```bash
java -Dedc.fs.config=policy/policy-02-provision/policy-provision-consumer/config.properties -jar policy/policy-02-provision/policy-provision-consumer/build/libs/consumer.jar --log-level=DEBUG
```

### Samples steps

When running this sample, knowledge about how to create an offer on the provider side and how to negotiate a contract
and start a transfer as a consumer is assumed. If you are not familiar with these processes yet, please have a look
at the [transfer samples](../../transfer/README.md).

#### Step 1: Create the asset

```bash
curl -d @policy/policy-02-provision/resources/create-asset.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/assets -s | jq
```

#### Step 2: Create the policy

In the policy, we add a permission with a single constraint, required the location to be within the EU.

```bash
curl -d @policy/policy-02-provision/resources/create-policy.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/policydefinitions -s | jq
```

#### Step 3: Create the contract definition

```bash
curl -d @policy/policy-02-provision/resources/create-contract-definition.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/contractdefinitions -s | jq
```

#### Step 4: Start a contract negotiation

```bash
curl -d @policy/policy-02-provision/resources/contract-request.json \
  -H 'X-Api-Key: password' \
  -X POST -H 'Content-Type: application/json' \
  http://localhost:29193/management/v3/contractnegotiations -s | jq
```

#### Step 5: Retrieve the contract agreement ID

To check the status and retrieve the contract agreement ID, use the negotiation ID from the previous step.

```bash
curl -X GET "http://localhost:29193/management/v3/contractnegotiations/{{contract-negotiation-id}}" \
    -H 'X-Api-Key: password' \
    --header 'Content-Type: application/json' -s | jq
```

#### Step 6: Initiate the transfer

Before running the next command, replace `{{contract-agreement-id}}` in
[transfer-request.json](resources/transfer-request.json) with the agreement ID from the previous step.

```bash
curl -d @policy/policy-02-provision/resources/transfer-request.json \
  -H 'X-Api-Key: password' \
  -X POST -H 'Content-Type: application/json' \
  http://localhost:29193/management/v3/transferprocesses -s | jq
```

In the transfer request, we specify a data destination of type `AmazonS3`. We have not yet created any S3 bucket in
LocalStack, but the consumer connector will take care of this during the provisioning process. Note, we in our
request we specify `us-east-1` as the bucket's location, which according to our policy is not allowed. The bucket
location will be changed by our policy function, so that the bucket will actually be provisioned in a different region.

#### Step 7: Verify transfer status

Use the transfer process ID to verify the file transfer status.

```bash
curl -H 'X-Api-Key: password' http://localhost:29193/management/v3/transferprocesses/<transfer-process-id> -s | jq
```

If the state is `COMPLETED`, continue with the next step.

#### Step 8: Verify the file in LocalStack S3

After the transfer has been completed, we should see the transferred file in the S3 bucket created by the consumer
connector.

```bash
aws --endpoint-url=http://localhost:4566 s3 ls s3://consumer-bucket
```

Expected Output:
```json
{
    "Key": "test-document.txt"
}
```

#### Step 9: Retrieve the region of a bucket

So, the transfer itself has already worked, but we have not yet verified whether the S3 bucket was created in accordance
with the policy. Thus, we now want to verify that the bucket has been created in the `eu-central-1` region and not in 
`us-east-1`, as set in the transfer request.

```bash
aws --endpoint-url http://localhost:4566 s3api get-bucket-location --bucket consumer-bucket
```

Expected Output:
```json
{
    "LocationConstraint": "eu-central-1"
}
```

Seeing that our bucket was created in the `eu-central-1` region means, that we have successfully utilized policy
enforcement to modify how resources are provisioned, so that they fulfil the constraints of a policy.

---
