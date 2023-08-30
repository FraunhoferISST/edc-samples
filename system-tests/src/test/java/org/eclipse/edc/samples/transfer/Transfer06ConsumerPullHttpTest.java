package org.eclipse.edc.samples.transfer;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.samples.transfer.FileTransferSampleTestCommon.getFileFromRelativePath;

@EndToEndTest
public class Transfer06ConsumerPullHttpTest {

    static final String CONSUMER_CONFIG_PROPERTIES_FILE_PATH = "transfer/transfer-06-consumer-pull-http/http-pull-consumer/consumer-configuration.properties";

    static final String PROVIDER_CONFIG_PROPERTIES_FILE_PATH = "transfer/transfer-06-consumer-pull-http/http-pull-provider/provider-configuration.properties";
    static final String SAMPLE_ASSET_FILE_PATH = "transfer/transfer-06-consumer-pull-http/README.md";
    static final Duration DURATION = Duration.ofSeconds(15);
    static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    @RegisterExtension
    static EdcRuntimeExtension provider = new EdcRuntimeExtension(
            ":transfer:transfer-06-consumer-pull-http:http-pull-provider",
            "provider",
            Map.of(
                    // Override 'edc.samples.transfer.01.asset.path' implicitly set via property 'edc.fs.config'.
                    "edc.samples.transfer.06.asset.path", getFileFromRelativePath(SAMPLE_ASSET_FILE_PATH).getAbsolutePath(),
                    "edc.fs.config", getFileFromRelativePath(PROVIDER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath()
            )
    );

    @RegisterExtension
    static EdcRuntimeExtension consumer = new EdcRuntimeExtension(
            ":transfer:transfer-06-consumer-pull-http:http-pull-consumer",
            "consumer",
            Map.of(
                    "edc.fs.config", getFileFromRelativePath(CONSUMER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath()
            )
    );

    final ConsumerPullSampleTestCommon testUtils =new ConsumerPullSampleTestCommon();

    @Test
    void runSampleSteps() throws Exception {
        testUtils.createAsset();
        testUtils.createPolicyDefinition();
        testUtils.createContractDefinition();
        testUtils.fetchCatalog();
        testUtils.initiateContractNegotiation();
        testUtils.getContractAgreement(testUtils.ContractNegotiationId);
        testUtils.createTransferProcess(testUtils.ContractAgreementId);
        testUtils.getTransferProcess(testUtils.TransferProcessId);
    }




}
