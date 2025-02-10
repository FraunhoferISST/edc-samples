/*
 *  Copyright (c) 2024 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package org.eclipse.edc.samples.policy;

import org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcessStates;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EmbeddedRuntime;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.eclipse.edc.junit.extensions.RuntimePerClassExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetBucketLocationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLocationResponse;

import java.time.Duration;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileFromRelativePath;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Testcontainers
@EndToEndTest
public class PolicyProvisionE2ETest {

    private static final String EDC_FS_CONFIG = "edc.fs.config";
    private static final String CONSUMER_CONFIG_PROPERTIES_FILE_PATH = "policy/policy-02-provision/policy-provision-consumer/config.properties";
    private static final String PROVIDER = "provider";
    private static final String CONSUMER = "consumer";
    private static final String PROVIDER_MODULE_PATH = ":policy:policy-02-provision:policy-provision-provider";
    private static final String CONSUMER_MODULE_PATH = ":policy:policy-02-provision:policy-provision-consumer";
    private static final String DESIRED_REGION = "eu-central-1";
    private static final String LOCALSTACK_IMAGE_NAME = "localstack/localstack:3.5.0";
    private static final String BUCKET_NAME = "consumer-bucket";

    @Container
    protected static LocalStackContainer localStackContainer = new LocalStackContainer(DockerImageName.parse(LOCALSTACK_IMAGE_NAME))
            .withServices(LocalStackContainer.Service.S3);

    @RegisterExtension
    protected static RuntimeExtension consumer = new RuntimePerClassExtension(new EmbeddedRuntime(
            CONSUMER,
            Map.of(
                    EDC_FS_CONFIG, getFileFromRelativePath(CONSUMER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath()
            ),
            CONSUMER_MODULE_PATH
    ));

    @RegisterExtension
    protected static RuntimeExtension provider = new RuntimePerClassExtension(new EmbeddedRuntime(
            PROVIDER,
            Map.ofEntries(
                    entry("edc.participant.id", "provider"),
                    entry("edc.dsp.callback.address", "http://localhost:8282/protocol"),
                    entry("web.http.port", "8181"),
                    entry("web.http.path", "/api"),
                    entry("web.http.management.port", "8182"),
                    entry("web.http.management.path", "/management"),
                    entry("web.http.protocol.port", "8282"),
                    entry("web.http.protocol.path", "/protocol"),
                    entry("web.http.public.port", "8185"),
                    entry("web.http.public.path", "/public"),
                    entry("web.http.control.port", "8183"),
                    entry("web.http.control.path", "/control"),
                    entry("edc.aws.access.key", localStackContainer.getAccessKey()),
                    entry("edc.aws.secret.access.key", localStackContainer.getSecretKey()),
                    entry("edc.aws.region", localStackContainer.getRegion())
            ),
            PROVIDER_MODULE_PATH
    ));

    @BeforeAll
    static void setup() {
        localStackContainer.start();
        String endpoint = localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3).toString();
        provider.setConfiguration(Map.of("edc.aws.endpoint.override", endpoint));
        System.out.println("LocalStack S3 Endpoint: " + endpoint);
    }

    @Test
    void testPolicyProvision() throws Exception {
        registerAsset();
        defineAccessPolicy();
        createContractDefinition();

        var contractAgreementId = runNegotiation();
        var transferProcessId = startTransfer(contractAgreementId);

        checkTransferStatus(transferProcessId, TransferProcessStates.COMPLETED);

        verifyBucketRegion();

    }

    private void registerAsset() {
        String assetJson = getFileContentFromRelativePath("policy/policy-02-provision/resources/create-asset.json");
        given()
                .baseUri("http://localhost:8182")
                .basePath("/management/v3/assets")
                .header("X-Api-Key", "password")
                .contentType("application/json")
                .body(assetJson)
                .when()
                .post()
                .then()
                .statusCode(200);
    }

    private void defineAccessPolicy() {
        String policyJson = getFileContentFromRelativePath("policy/policy-02-provision/resources/create-policy.json");
        given()
                .baseUri("http://localhost:8182")
                .basePath("/management/v3/policydefinitions")
                .header("X-Api-Key", "password")
                .contentType("application/json")
                .body(policyJson)
                .when()
                .post()
                .then()
                .statusCode(200);
    }


    private void createContractDefinition() {
        String contractJson = getFileContentFromRelativePath("policy/policy-02-provision/resources/create-contract-definition.json");
        given()
                .baseUri("http://localhost:8182")
                .basePath("/management/v3/contractdefinitions")
                .header("X-Api-Key", "password")
                .contentType("application/json")
                .body(contractJson)
                .when()
                .post()
                .then()
                .statusCode(200);
    }

    private String runNegotiation() {
        String negotiationJson = getFileContentFromRelativePath("policy/policy-02-provision/resources/negotiate-contract.json");
        var negotiationId = given()
                .baseUri("http://localhost:9192")
                .basePath("/management/v3/contractnegotiations")
                .header("X-Api-Key", "password")
                .contentType("application/json")
                .body(negotiationJson)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("@id");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return given()
                .baseUri("http://localhost:9192")
                .basePath("/management/v3/contractnegotiations/" + negotiationId)
                .header("X-Api-Key", "password")
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("contractAgreementId");
    }

    private String startTransfer(String contractAgreementId) {
        String transferJson = getFileContentFromRelativePath("policy/policy-02-provision/resources/filetransfer.json");
        return given()
                .baseUri("http://localhost:9192")
                .basePath("/management/v3/transferprocesses")
                .header("X-Api-Key", "password")
                .contentType("application/json")
                .body("""
                        {
                            "@context": {
                                "@vocab": "https://w3id.org/edc/v0.0.1/ns/"
                            },
                            "@type": "TransferRequestDto",
                            "counterPartyId": "provider",
                            "counterPartyAddress": "http://localhost:8282/protocol",
                            "contractId": "%s",
                            "protocol": "dataspace-protocol-http",
                            "transferType": "AmazonS3-PUSH",
                            "dataDestination": {
                                "type": "AmazonS3",
                                "region": "%s",
                                "bucketName": "consumer-bucket",
                                "objectName": "test-document.txt",
                                "endpointOverride": "%s",
                                "accessKeyId": "%s",
                                "secretAccessKey": "%s"
                            }
                        }
                        """.formatted(
                        contractAgreementId,
                        localStackContainer.getRegion(),
                        localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3),
                        localStackContainer.getAccessKey(),
                        localStackContainer.getSecretKey()))
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("@id");
    }

    private void checkTransferStatus(String transferProcessId, TransferProcessStates expectedState) {
        await()
                .atMost(Duration.ofSeconds(60))
                .pollDelay(Duration.ofMillis(1000))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    var response = given()
                            .baseUri("http://localhost:9192")
                            .basePath("/management/v3/transferprocesses")
                            .header("X-Api-Key", "password")
                            .when()
                            .get("/" + transferProcessId)
                            .then()
                            .statusCode(200)
                            .extract()
                            .body()
                            .jsonPath();

                    var currentState = response.getString("state");
                    assertThat(currentState).isEqualTo(expectedState.name());
                });
    }


    private void verifyBucketRegion() {
        S3Client s3Client = S3Client.builder()
                .endpointOverride(localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3))
                .region(Region.of(DESIRED_REGION))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("admin", "password")))
                .build();

        GetBucketLocationResponse location = s3Client.getBucketLocation(GetBucketLocationRequest.builder().bucket(BUCKET_NAME).build());
        assertThat(location.locationConstraintAsString()).isEqualTo(DESIRED_REGION);
    }
}