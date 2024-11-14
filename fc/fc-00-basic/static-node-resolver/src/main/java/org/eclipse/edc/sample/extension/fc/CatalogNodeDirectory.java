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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.crawler.spi.TargetNode;
import org.eclipse.edc.crawler.spi.TargetNodeDirectory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CatalogNodeDirectory implements TargetNodeDirectory {

    private final ObjectMapper objectMapper;

    public CatalogNodeDirectory() {
        this.objectMapper = new ObjectMapper();
    }

    public List<TargetNode> readNodesFromJson() {
        try {
            String participant_directory = "fc/fc-00-basic/static-node-resolver/catalog-node-directory.json";
            return objectMapper.readValue(new File(participant_directory), new TypeReference<List<TargetNode>>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<TargetNode> getAll() {
        return readNodesFromJson();
    }

    @Override
    public void insert(TargetNode targetNode) {

    }
}
