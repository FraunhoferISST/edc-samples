package org.eclipse.edc.samples.transfer;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static org.eclipse.edc.samples.transfer.FileTransferSampleTestCommon.getFileFromRelativePath;

//@EndToEndTest
public class Transfer07ProviderPushHttpTest {

    static final String CONSUMER_CONFIG_PROPERTIES_FILE_PATH = "transfer/transfer-07-provider-push-http/http-push-consumer/consumer-configuration.properties";
    static final String PROVIDER_CONFIG_PROPERTIES_FILE_PATH = "transfer/transfer-07-provider-push-http/http-push-provider/provider-configuration.properties";
    static final String SAMPLE_CERT_FILE_PATH = "transfer/transfer-07-provider-push-http/certs/cert.pfx";
    static final String SAMPLE_ASSET_FILE_PATH = "transfer/transfer-07-provider-push-http/README.md";
    static final String PROVIDER_VAULT_PROPERTIES_FILE_PATH = "transfer/transfer-07-provider-push-http/http-push-provider/provider-vault.properties";
    static final String CONSUMER_VAULT_PROPERTIES_FILE_PATH = "transfer/transfer-07-provider-push-http/http-push-consumer/consumer-vault.properties";
    String contractNegotiationId;
    String contractAgreementId;
    String transferProcessId;
    
    @RegisterExtension
    static EdcRuntimeExtension provider = new EdcRuntimeExtension(
            ":transfer:transfer-07-provider-push-http/http-push-connector",
            "provider",
            Map.of(
                    // Override 'edc.samples.transfer.01.asset.path' implicitly set via property 'edc.fs.config'.
                    "edc.samples.transfer.07.asset.path", getFileFromRelativePath(SAMPLE_ASSET_FILE_PATH).getAbsolutePath(),
                    "edc.fs.config", getFileFromRelativePath(PROVIDER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath(),
                    "edc.keystore", getFileFromRelativePath(SAMPLE_CERT_FILE_PATH).getAbsolutePath(),
                    "edc.keystore.password", "12345",
                    "edc.vault", getFileFromRelativePath(PROVIDER_VAULT_PROPERTIES_FILE_PATH).getAbsolutePath()


            )
    );
    @RegisterExtension
    static EdcRuntimeExtension consumer = new EdcRuntimeExtension(
            ":transfer:transfer-07-provider-push-http/http-push-connector",
            "consumer",
            Map.of(
                    "edc.samples.transfer.07.asset.path", getFileFromRelativePath(SAMPLE_ASSET_FILE_PATH).getAbsolutePath(),
                    "edc.fs.config", getFileFromRelativePath(CONSUMER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath(),
                    "edc.keystore.password", "12345",
                    "edc.keystore", getFileFromRelativePath(SAMPLE_CERT_FILE_PATH).getAbsolutePath(),
                    "edc.vault", getFileFromRelativePath(CONSUMER_VAULT_PROPERTIES_FILE_PATH).getAbsolutePath()

            )
    );

    //@Before
    public void startServer() throws Exception {
        var command = "HTTP_SERVER_PORT=4000 java -jar util/http-request-logger/build/libs/http-request-logger.jar";
        Process serverProcess = Runtime.getRuntime().exec(command);
        Thread.sleep(2000);
    }

    final ProviderPushSampleTestCommon testUtils= new ProviderPushSampleTestCommon();

    @Test
    void runSampleSteps() throws Exception {
        testUtils.registerDataPlaneInstanceProvider();
        testUtils.createAsset();
        testUtils.createPolicyDefinition();
        testUtils.createContractDefinition();
        testUtils.fetchCatalog();
        contractNegotiationId= testUtils.initiateContractNegotiation();
        contractAgreementId= testUtils.getContractAgreement(contractNegotiationId);
        transferProcessId= testUtils.createTransferProcess(contractAgreementId);
        testUtils.getTransferProcess(transferProcessId);
    }




}
