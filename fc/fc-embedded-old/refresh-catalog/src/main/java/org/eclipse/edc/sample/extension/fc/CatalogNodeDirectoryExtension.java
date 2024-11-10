package org.eclipse.edc.sample.extension.fc;

import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.iam.IdentityService;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;

public class CatalogNodeDirectoryExtension implements ServiceExtension {
    @Setting
    private static final String REGISTRATION_SERVICE_API_URL = "registration.service.api.url";

    @Inject
    private Monitor monitor;

    @Inject
    private TypeManager typeManager;

    @Inject
    private IdentityService identityService;

    private TargetNodeDirectory nodeDirectory;


    @Provider 
    public TargetNodeDirectory federatedCacheNodeDirectory() {
        if (nodeDirectory == null) {
            nodeDirectory = new CatalogNodeDirectory();
        }
        return nodeDirectory;
    }

}
