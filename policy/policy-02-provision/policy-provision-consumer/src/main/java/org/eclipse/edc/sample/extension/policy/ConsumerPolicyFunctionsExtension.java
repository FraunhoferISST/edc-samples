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
import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.policy.engine.spi.RuleBindingRegistry;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import static org.eclipse.edc.connector.controlplane.transfer.spi.policy.ProvisionManifestVerifyPolicyContext.MANIFEST_VERIFICATION_SCOPE;

@Extension(value = ConsumerPolicyFunctionsExtension.NAME)
public class ConsumerPolicyFunctionsExtension implements ServiceExtension {
    public static final String NAME = "Consumer Policy Functions Extension";
    public static final String KEY = "https://w3id.org/edc/v0.0.1/ns/location";

    @Inject
    private Monitor monitor;
    @Inject
    private RuleBindingRegistry ruleBindingRegistry;
    @Inject
    private PolicyEngine policyEngine;
    @Inject
    private Vault vault;

    @Override
    public void initialize(ServiceExtensionContext context) {
        ruleBindingRegistry.bind("http://www.w3.org/ns/odrl/2/use", MANIFEST_VERIFICATION_SCOPE);
        ruleBindingRegistry.bind(KEY, MANIFEST_VERIFICATION_SCOPE);

        policyEngine.registerFunction(ProvisionManifestVerifyPolicyContext.class, Permission.class, KEY, new RegionConstraintFunction(monitor));

        // store credentials for accessing the Localstack container
        vault.storeSecret("accessKeyId", "admin");
        vault.storeSecret("secretAccessKey", "password");
    }

    @Override
    public String name() {
        return NAME;
    }
}
