/*
 *  Copyright (c) 2025 Fraunhofer Institute for Software and Systems Engineering
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

package org.eclipse.edc.sample.extension.policy;

import org.eclipse.edc.connector.controlplane.transfer.spi.policy.ProvisionManifestVerifyPolicyContext;
import org.eclipse.edc.connector.provision.aws.s3.S3BucketResourceDefinition;
import org.eclipse.edc.policy.engine.spi.AtomicConstraintRuleFunction;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.spi.monitor.Monitor;

import static java.lang.String.format;

public class RegionConstraintFunction implements AtomicConstraintRuleFunction<Permission, ProvisionManifestVerifyPolicyContext> {
    
    private static final String DEFAULT_REGION = "eu-central-1";
    
    private final Monitor monitor;

    public RegionConstraintFunction(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public boolean evaluate(Operator operator, Object rightValue, Permission rule, ProvisionManifestVerifyPolicyContext context) {
        var rightValueString = (String) rightValue;
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
    }
}
