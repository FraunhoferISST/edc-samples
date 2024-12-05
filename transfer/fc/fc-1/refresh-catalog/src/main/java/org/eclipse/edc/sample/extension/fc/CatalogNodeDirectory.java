package org.eclipse.edc.sample.extension.fc;

import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.edc.crawler.spi.TargetNode;
import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CatalogNodeDirectory implements TargetNodeDirectory {

    private final ObjectMapper objectMapper;

    public CatalogNodeDirectory() {
        this.objectMapper = new ObjectMapper();
    }

    public List<TargetNode> readNodesFromJson() {
        try {
            List<TargetNode> nodes = objectMapper.readValue(new File("transfer/fc/fc-1/refresh-catalog/catalog-node-directory.json"), new TypeReference<List<TargetNode>>(){});
            return nodes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<TargetNode> getAll() {
        List<TargetNode> nodes = readNodesFromJson();
        return nodes;
    }

    @Override
    public void insert(TargetNode targetNode) {

    }
}
