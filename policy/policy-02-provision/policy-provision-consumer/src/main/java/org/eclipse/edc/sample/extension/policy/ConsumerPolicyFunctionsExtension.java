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

import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.policy.engine.spi.RuleBindingRegistry;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

//import static org.eclipse.edc.connector.controlplane.transfer.spi.provision.ResourceManifestGenerator.MANIFEST_VERIFICATION_SCOPE;
import static org.eclipse.edc.policy.engine.spi.PolicyEngine.ALL_SCOPES;

@Extension(value = ConsumerPolicyFunctionsExtension.NAME)
public class ConsumerPolicyFunctionsExtension implements ServiceExtension {
    public static final String NAME = "Consumer Policy Functions Extension";
    public static final String KEY = "POLICY_REGULATE_FILE_PATH";
    public static final String MANIFEST_VERIFICATION_SCOPE = "provision.manifest.verify";

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
        ruleBindingRegistry.bind("USE", ALL_SCOPES);
        ruleBindingRegistry.bind(KEY, MANIFEST_VERIFICATION_SCOPE);
        policyEngine.registerFunction(MANIFEST_VERIFICATION_SCOPE, Permission.class, KEY, new RegulateFilePathFunction(monitor));

        vault.storeSecret("S3AccessKey", "admin");
        vault.storeSecret("S3SecretKey", "password");
    }

    @Override
    public String name() {
        return NAME;
    }

}