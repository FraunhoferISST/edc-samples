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

import org.eclipse.edc.connector.controlplane.transfer.spi.provision.ResourceManifestContext;
import org.eclipse.edc.connector.provision.aws.s3.S3BucketResourceDefinition;
import org.eclipse.edc.policy.engine.spi.AtomicConstraintFunction;
import org.eclipse.edc.policy.engine.spi.PolicyContext;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.spi.monitor.Monitor;

import java.util.Objects;

public class RegulateFilePathFunction implements AtomicConstraintFunction<Permission> {
    private final Monitor monitor;

    public RegulateFilePathFunction(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public boolean evaluate(Operator operator, Object rightValue, Permission rule, PolicyContext context) {
        var desiredRegion = (String) rightValue;

        if (Objects.requireNonNull(operator) == Operator.EQ) {
            var manifestContext = context.getContextData(ResourceManifestContext.class);

            manifestContext.getDefinitions().stream()
                    .filter(S3BucketResourceDefinition.class::isInstance)
                    .map(S3BucketResourceDefinition.class::cast)
                    .forEach(definition -> {
                        definition.toBuilder().regionId(desiredRegion).build();
                    });

            return true;
        }

        monitor.debug(String.format("Operator expected to be EQ but was %s", operator));
        return false;
    }
}
