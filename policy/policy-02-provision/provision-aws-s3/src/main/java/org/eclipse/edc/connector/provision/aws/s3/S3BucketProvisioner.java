/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.edc.connector.provision.aws.s3;

import org.eclipse.edc.aws.s3.AwsClientProvider;
import org.eclipse.edc.aws.s3.S3ClientRequest;
import org.eclipse.edc.connector.controlplane.transfer.spi.provision.Provisioner;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.DeprovisionedResource;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.ProvisionResponse;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.ProvisionedResource;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.ResourceDefinition;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.response.StatusResult;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.util.concurrent.CompletableFuture;

/**
 * Asynchronously provisions S3 buckets.
 */
public class S3BucketProvisioner implements Provisioner<S3BucketResourceDefinition, S3BucketProvisionedResource> {

    private final AwsClientProvider clientProvider;
    private final Monitor monitor;

    public S3BucketProvisioner(AwsClientProvider clientProvider, Monitor monitor) {
        this.clientProvider = clientProvider;
        this.monitor = monitor;
    }

    @Override
    public boolean canProvision(ResourceDefinition resourceDefinition) {
        return resourceDefinition instanceof S3BucketResourceDefinition;
    }

    @Override
    public boolean canDeprovision(ProvisionedResource resourceDefinition) {
        return resourceDefinition instanceof S3BucketProvisionedResource;
    }

    @Override
    public CompletableFuture<StatusResult<ProvisionResponse>> provision(S3BucketResourceDefinition resourceDefinition, Policy policy) {
        var rq = S3ClientRequest.from(resourceDefinition.getRegionId(), resourceDefinition.getEndpointOverride());
        var s3AsyncClient = clientProvider.s3AsyncClient(rq);
    
        var request = CreateBucketRequest.builder()
                .bucket(resourceDefinition.getBucketName())
                .createBucketConfiguration(CreateBucketConfiguration.builder().build())
                .build();
    
        monitor.debug("S3Provisioner: create bucket " + resourceDefinition.getBucketName());
        return s3AsyncClient.createBucket(request)
                .thenApply(response -> provisionSucceeded(resourceDefinition));
    }

    @Override
    public CompletableFuture<StatusResult<DeprovisionedResource>> deprovision(S3BucketProvisionedResource resource, Policy policy) {
        return CompletableFuture.completedFuture(StatusResult.success(DeprovisionedResource.Builder.newInstance()
                .provisionedResourceId(resource.getId())
                .build()));
    }

    private StatusResult<ProvisionResponse> provisionSucceeded(S3BucketResourceDefinition resourceDefinition) {
        var resource = S3BucketProvisionedResource.Builder.newInstance()
                .id(resourceDefinition.getBucketName())
                .resourceDefinitionId(resourceDefinition.getId())
                .hasToken(true)
                .region(resourceDefinition.getRegionId())
                .bucketName(resourceDefinition.getBucketName())
                .objectName(resourceDefinition.getObjectName())
                .transferProcessId(resourceDefinition.getTransferProcessId())
                .resourceName(resourceDefinition.getBucketName())
                .endpointOverride(resourceDefinition.getEndpointOverride())
                .accessKeyId(resourceDefinition.getAccessKeyId())
                .secretAccessKey(resourceDefinition.getSecretAccessKey())
                .build();
        
        monitor.debug("S3BucketProvisioner: Bucket created successfully: " + resourceDefinition.getBucketName());
        var response = ProvisionResponse.Builder.newInstance().resource(resource).build();
        return StatusResult.success(response);
    }
}


