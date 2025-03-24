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
import org.eclipse.edc.samples.common.PrerequisitesCommon;
import org.eclipse.edc.spi.system.configuration.Config;
import org.eclipse.edc.spi.system.configuration.ConfigFactory;
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

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileFromRelativePath;
import static org.eclipse.edc.samples.common.NegotiationCommon.getContractAgreementId;
import static org.eclipse.edc.samples.common.NegotiationCommon.negotiateContract;
import static org.eclipse.edc.samples.common.PolicyCommon.createAsset;
import static org.eclipse.edc.samples.common.PolicyCommon.createContractDefinition;
import static org.eclipse.edc.samples.common.PolicyCommon.createPolicy;
import static org.eclipse.edc.samples.util.TransferUtil.checkTransferStatus;
import static org.eclipse.edc.samples.util.TransferUtil.post;

@Testcontainers
@EndToEndTest
public class Policy02ProvisionTest {
    
    private static final String LOCALSTACK_IMAGE_NAME = "localstack/localstack:4.2.0";
    
    private static final String PROVIDER_ID = "provider";
    private static final String CONSUMER_ID = "consumer";
    private static final String PROVIDER_CONFIG_PROPERTIES_FILE_PATH = "policy/policy-02-provision/policy-provision-provider/config.properties";
    private static final String CONSUMER_CONFIG_PROPERTIES_FILE_PATH = "policy/policy-02-provision/policy-provision-consumer/config.properties";
    private static final String PROVIDER_MODULE_PATH = ":policy:policy-02-provision:policy-provision-provider";
    private static final String CONSUMER_MODULE_PATH = ":policy:policy-02-provision:policy-provision-consumer";
    
    private static final String SAMPLE_FOLDER = "policy/policy-02-provision";
    private static final String CREATE_ASSET_FILE_PATH = SAMPLE_FOLDER + "/resources/create-asset.json";
    private static final String CREATE_POLICY_FILE_PATH = SAMPLE_FOLDER + "/resources/create-policy.json";
    private static final String CREATE_CONTRACT_DEFINITION_FILE_PATH = SAMPLE_FOLDER + "/resources/create-contract-definition.json";
    private static final String CONTRACT_REQUEST_FILE_PATH = SAMPLE_FOLDER + "/resources/contract-request.json";
    private static final String TRANSFER_REQUEST_FILE_PATH = SAMPLE_FOLDER + "/resources/transfer-request.json";
    private static final String CONTRACT_AGREEMENT_ID_KEY = "{{contract-agreement-id}}";
    private static final String V3_TRANSFER_PROCESS_PATH = "/v3/transferprocesses/";
    
    private static final String DESIRED_REGION = "eu-central-1";
    private static final String BUCKET_NAME = "consumer-bucket";

    @Container
    private static final LocalStackContainer LOCAL_STACK_CONTAINER = new LocalStackContainer(DockerImageName.parse(LOCALSTACK_IMAGE_NAME))
            .withServices(
                    LocalStackContainer.Service.S3,
                    LocalStackContainer.Service.IAM,
                    LocalStackContainer.Service.STS);
    
    @RegisterExtension
    private static final RuntimeExtension PROVIDER = new RuntimePerClassExtension(new EmbeddedRuntime(
            PROVIDER_ID, PROVIDER_MODULE_PATH)
            .configurationProvider(() -> loadConfig(PROVIDER_CONFIG_PROPERTIES_FILE_PATH)));
    
    @RegisterExtension
    private static final RuntimeExtension CONSUMER = new RuntimePerClassExtension(new EmbeddedRuntime(
            CONSUMER_ID, CONSUMER_MODULE_PATH)
            .configurationProvider(() -> loadConfig(CONSUMER_CONFIG_PROPERTIES_FILE_PATH)));
    
    @BeforeAll
    static void setup() {
        LOCAL_STACK_CONTAINER.start();
        var endpoint = LOCAL_STACK_CONTAINER.getEndpoint().toString();
        PROVIDER.setConfiguration(Map.of("edc.aws.endpoint.override", endpoint));
        CONSUMER.setConfiguration(Map.of("edc.aws.endpoint.override", endpoint));
    }

    @Test
    void testPolicyProvision() {
        createAsset(CREATE_ASSET_FILE_PATH);
        createPolicy(CREATE_POLICY_FILE_PATH);
        createContractDefinition(CREATE_CONTRACT_DEFINITION_FILE_PATH);
        var negotiationId = negotiateContract(CONTRACT_REQUEST_FILE_PATH, "");
        var agreementId = getContractAgreementId(negotiationId);
        var transferProcessId = initiateTransfer(agreementId);
        checkTransferStatus(transferProcessId, TransferProcessStates.COMPLETED);

        verifyBucketRegion();
    }
    
    private String initiateTransfer(String agreementId) {
        var requestBody = getFileContentFromRelativePath(TRANSFER_REQUEST_FILE_PATH)
                .replace(CONTRACT_AGREEMENT_ID_KEY, agreementId)
                .replace("http://localhost:4566", LOCAL_STACK_CONTAINER.getEndpoint().toString())
                .replace("admin", LOCAL_STACK_CONTAINER.getAccessKey())
                .replace("password", LOCAL_STACK_CONTAINER.getSecretKey());
        
        var transferProcessId = post(
                PrerequisitesCommon.CONSUMER_MANAGEMENT_URL + V3_TRANSFER_PROCESS_PATH,
                requestBody,
                "@id"
        );
        
        assertThat(transferProcessId).isNotEmpty();
        return transferProcessId;
    }
    
    private void verifyBucketRegion() {
        var credentials = AwsBasicCredentials.create(LOCAL_STACK_CONTAINER.getAccessKey(), LOCAL_STACK_CONTAINER.getSecretKey());
        var s3Client = S3Client.builder()
                .endpointOverride(LOCAL_STACK_CONTAINER.getEndpointOverride(LocalStackContainer.Service.S3))
                .region(Region.of(DESIRED_REGION))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        var location = s3Client.getBucketLocation(GetBucketLocationRequest.builder().bucket(BUCKET_NAME).build());
        assertThat(location.locationConstraintAsString()).isEqualTo(DESIRED_REGION);
    }
    
    private static Config loadConfig(String configFilePath) {
        var file = getFileFromRelativePath(configFilePath).getAbsolutePath();
        var properties = new Properties();
        
        try (var input = new FileInputStream(file)) {
            properties.load(input);
            var map = new HashMap<String, String>();
            
            for (var key : properties.stringPropertyNames()) {
                map.put(key, properties.getProperty(key));
            }
            
            return ConfigFactory.fromMap(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}