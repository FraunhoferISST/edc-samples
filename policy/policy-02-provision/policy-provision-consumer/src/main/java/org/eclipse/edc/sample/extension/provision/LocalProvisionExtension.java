/*
 *  Copyright (c) 2023 Fraunhofer Institute for Software and Systems Engineering
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

package org.eclipse.edc.sample.extension.provision;

import dev.failsafe.RetryPolicy;
import org.eclipse.edc.aws.s3.AwsClientProvider;
import org.eclipse.edc.aws.s3.AwsClientProviderConfiguration;
import org.eclipse.edc.aws.s3.AwsClientProviderImpl;
import org.eclipse.edc.connector.controlplane.transfer.spi.provision.ProvisionManager;
import org.eclipse.edc.connector.controlplane.transfer.spi.provision.ResourceManifestGenerator;
import org.eclipse.edc.connector.provision.aws.s3.S3BucketProvisioner;
import org.eclipse.edc.connector.provision.aws.s3.S3BucketProvisionerConfiguration;
import org.eclipse.edc.connector.provision.aws.s3.S3ConsumerResourceDefinitionGenerator;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import java.net.URI;
import java.time.Duration;

@Extension(value = LocalProvisionExtension.NAME)
public class LocalProvisionExtension implements ServiceExtension {
    public static final String NAME = "Local Provision Extension";
    @Setting
    private static final String PROVISION_MAX_RETRY = "10";
    @Setting
    private static final String ROLE_MAX_SESSION_DURATION = "3600";
    @Inject
    private Monitor monitor;
    @Inject
    private ProvisionManager provisionManager;
    @Inject
    private ResourceManifestGenerator manifestGenerator;
    @Inject
    private AwsClientProvider awsClientProvider;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        String accessKeyId = context.getSetting("edc.aws.access.key", "admin");
        String secretAccessKey = context.getSetting("edc.aws.secret.access.key", "password");

        int maxRetries = context.getSetting(PROVISION_MAX_RETRY, 10);
        int roleMaxSessionDuration = context.getSetting(ROLE_MAX_SESSION_DURATION, 3600);

        AwsClientProviderConfiguration awsConfig = AwsClientProviderConfiguration.Builder.newInstance()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .endpointOverride(URI.create("http://localhost:9000"))
                .build();

        AwsClientProvider awsClientProvider = new AwsClientProviderImpl(awsConfig);

        RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
                .handle(Exception.class)
                .withMaxRetries(maxRetries)
                .withDelay(Duration.ofSeconds(2))
                .build();

        S3BucketProvisionerConfiguration provisionerConfiguration = new S3BucketProvisionerConfiguration(maxRetries, roleMaxSessionDuration);

        S3BucketProvisioner s3BucketProvisioner = new S3BucketProvisioner(awsClientProvider, monitor, retryPolicy, provisionerConfiguration);
        provisionManager.register(s3BucketProvisioner);

        manifestGenerator.registerGenerator(new S3ConsumerResourceDefinitionGenerator());
    }

}