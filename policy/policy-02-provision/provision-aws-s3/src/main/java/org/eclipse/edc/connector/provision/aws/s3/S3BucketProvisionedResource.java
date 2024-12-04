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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.eclipse.edc.aws.s3.spi.S3BucketSchema;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.ProvisionedDataDestinationResource;

import static org.eclipse.edc.aws.s3.spi.S3BucketSchema.ACCESS_KEY_ID;
import static org.eclipse.edc.aws.s3.spi.S3BucketSchema.BUCKET_NAME;
import static org.eclipse.edc.aws.s3.spi.S3BucketSchema.ENDPOINT_OVERRIDE;
import static org.eclipse.edc.aws.s3.spi.S3BucketSchema.REGION;
import static org.eclipse.edc.aws.s3.spi.S3BucketSchema.SECRET_ACCESS_KEY;


/**
 * A provisioned S3 bucket and credentials associated with a transfer process.
 */
@JsonDeserialize(builder = S3BucketProvisionedResource.Builder.class)
@JsonTypeName("dataspaceconnector:s3bucketprovisionedresource")
public class S3BucketProvisionedResource extends ProvisionedDataDestinationResource {

    public String getRegion() {
        return getDataAddress().getStringProperty(REGION);
    }

    public String getBucketName() {
        return getDataAddress().getStringProperty(BUCKET_NAME);
    }

    public String getEndpointOverride() {
        return getDataAddress().getStringProperty(ENDPOINT_OVERRIDE);
    }

    @Override
    public String getResourceName() {
        return dataAddress.getStringProperty(BUCKET_NAME);
    }

    private S3BucketProvisionedResource() {
    }


    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends ProvisionedDataDestinationResource.Builder<S3BucketProvisionedResource, Builder> {

        private Builder() {
            super(new S3BucketProvisionedResource());
            dataAddressBuilder.type(S3BucketSchema.TYPE);
        }

        @JsonCreator
        public static Builder newInstance() {
            return new Builder();
        }

        public Builder region(String region) {
            dataAddressBuilder.property(REGION, region);
            return this;
        }

        public Builder bucketName(String bucketName) {
            dataAddressBuilder.property(BUCKET_NAME, bucketName);
            return this;
        }

        public Builder endpointOverride(String endpointOverride) {
            dataAddressBuilder.property(ENDPOINT_OVERRIDE, endpointOverride);
            return this;
        }
        
        public Builder accessKeyId(String accessKeyId) {
            dataAddressBuilder.property(ACCESS_KEY_ID, accessKeyId);
            return this;
        }
    
        public Builder secretAccessKey(String secretAccessKey) {
            dataAddressBuilder.property(SECRET_ACCESS_KEY, secretAccessKey);
            return this;
        }
    }

}
