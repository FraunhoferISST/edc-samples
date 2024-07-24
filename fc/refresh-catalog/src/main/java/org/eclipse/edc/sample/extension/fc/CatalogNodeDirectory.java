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
            List<TargetNode> nodes = objectMapper.readValue(new File("/home/farhin/Projects/samples/Samples/fc/refresh-catalog/catalog-node-directory.json"), new TypeReference<List<TargetNode>>(){});
            return nodes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TargetNode> demoNodes() {
        List<String> supportedProtocols = new ArrayList<>();
        supportedProtocols.add("dataspace-protocol-http");

        var node1 = new TargetNode("https://w3id.org/edc/v0.0.1/ns/", "provider",
                "http://localhost:19194/protocol", supportedProtocols);
        var node2 = new TargetNode("https://w3id.org/edc/v0.0.1/ns/", "provider-2",
                "http://localhost:39194/protocol", supportedProtocols);
//        var node3 = new TargetNode("did:web:did-server:company3", "company3",
//                "http://company3:8282/api/dsp", supportedProtocols);

        List<TargetNode> listNodes = new ArrayList<>();
        listNodes.add(node1);
        listNodes.add(node2);
//        listNodes.add(node3);

        return listNodes;
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
