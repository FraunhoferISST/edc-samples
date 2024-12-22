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

package org.eclipse.edc.sample.extension.policy;

import org.eclipse.edc.connector.controlplane.transfer.spi.policy.ProvisionManifestVerifyPolicyContext;
import org.eclipse.edc.connector.provision.aws.s3.S3BucketResourceDefinition;
import org.eclipse.edc.policy.engine.spi.AtomicConstraintRuleFunction;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.spi.monitor.Monitor;

import java.util.Objects;

public class RegionConstraintFunction implements AtomicConstraintRuleFunction<Permission, ProvisionManifestVerifyPolicyContext> {
    private final Monitor monitor;

    public RegionConstraintFunction(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public boolean evaluate(Operator operator, Object rightValue, Permission rule, ProvisionManifestVerifyPolicyContext context) {
        monitor.info("Operator: " + operator + ", Right Value: " + rightValue);

        if (!(rightValue instanceof String)) {
            return false;
        }

        var manifestContext = context.resourceManifestContext();
        if (manifestContext == null) {
            return false;
        }

        var updatedDefinitions = manifestContext.getDefinitions(S3BucketResourceDefinition.class)
                .stream()
                .map(definition -> {
                    String region = definition.getRegionId();
                    if (Objects.requireNonNull(operator) == Operator.EQ) {
                        if (!region.startsWith("eu")) {
                            monitor.warning("Region does not start with 'eu'. Setting to default: eu-central-1.");
                            region = "eu-central-1";
                            return definition.toBuilder().regionId(region).build();
                        }
                    }
                    return definition;
                })
                .toList();

        manifestContext.replaceDefinitions(S3BucketResourceDefinition.class, updatedDefinitions);
        return true;
    }
}
