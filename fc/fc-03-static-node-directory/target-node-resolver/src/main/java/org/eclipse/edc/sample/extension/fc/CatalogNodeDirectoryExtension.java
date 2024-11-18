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
 *       Fraunhofer Institute for Software and Systems Engineering - added dependencies
 *
 */

package org.eclipse.edc.sample.extension.fc;

import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.io.File;

public class CatalogNodeDirectoryExtension implements ServiceExtension {

    private File participantListFile;
    private Monitor monitor;

    private TargetNodeDirectory nodeDirectory;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var participantsPath = "fc/fc-03-static-node-directory/target-node-resolver/catalog-node-directory.json";
        monitor = context.getMonitor().withPrefix("DEMO");

        participantListFile = new File(participantsPath).getAbsoluteFile();
        if (!participantListFile.exists()) {
            monitor.warning("Path '%s' does not exist.".formatted(participantsPath));
        }
    }

    @Provider 
    public TargetNodeDirectory federatedCacheNodeDirectory() {
        if (nodeDirectory == null) {
            nodeDirectory = new CatalogNodeDirectory(participantListFile);
        }
        return nodeDirectory;
    }

}
