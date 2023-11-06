package org.eclipse.edc.samples.transfer;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;


import static org.eclipse.edc.samples.transfer.FileTransferSampleTestCommon.getFileFromRelativePath;

//@EndToEndTest
public class Transfer06ConsumerPullHttpTest {

    static final String CONSUMER_CONFIG_PROPERTIES_FILE_PATH = "transfer/transfer-06-consumer-pull-http/http-pull-consumer/consumer-configuration.properties";

    static final String PROVIDER_CONFIG_PROPERTIES_FILE_PATH = "transfer/transfer-06-consumer-pull-http/http-pull-provider/provider-configuration.properties";
    static final String SAMPLE_CERT_FILE_PATH = "transfer/transfer-06-consumer-pull-http/certs/cert.pfx";
    static final String SAMPLE_ASSET_FILE_PATH = "transfer/transfer-06-consumer-pull-http/README.md";
    static final String PROVIDER_VAULT_PROPERTIES_FILE_PATH = "transfer/transfer-06-consumer-pull-http/http-pull-provider/provider-vault.properties";
    static final String CONSUMER_VAULT_PROPERTIES_FILE_PATH = "transfer/transfer-06-consumer-pull-http/http-pull-consumer/consumer-vault.properties";

    String contractOfferId;
    String contractNegotiationId;
    String contractAgreementId;
    String transferProcessId;
    private static InputStream serverLogStream = null;

    private Process serverProcess;

    @RegisterExtension
    static EdcRuntimeExtension provider = new EdcRuntimeExtension(
            ":transfer:transfer-06-consumer-pull-http:http-pull-connector",
            "provider",
            Map.of(
                    // Override 'edc.samples.transfer.01.asset.path' implicitly set via property 'edc.fs.config'.
                    "edc.samples.transfer.06.asset.path", getFileFromRelativePath(SAMPLE_ASSET_FILE_PATH).getAbsolutePath(),
                    "edc.fs.config", getFileFromRelativePath(PROVIDER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath(),
                    "edc.keystore", getFileFromRelativePath(SAMPLE_CERT_FILE_PATH).getAbsolutePath(),
                    "edc.keystore.password","12345",
                    "edc.vault", getFileFromRelativePath(PROVIDER_VAULT_PROPERTIES_FILE_PATH).getAbsolutePath()


            )
    );

    @RegisterExtension
    static EdcRuntimeExtension consumer = new EdcRuntimeExtension(
            ":transfer:transfer-06-consumer-pull-http:http-pull-connector",
            "consumer",
            Map.of(
                    "edc.samples.transfer.06.asset.path", getFileFromRelativePath(SAMPLE_ASSET_FILE_PATH).getAbsolutePath(),
                    "edc.fs.config", getFileFromRelativePath(CONSUMER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath(),
                    "edc.keystore.password","12345",
                    "edc.keystore", getFileFromRelativePath(SAMPLE_CERT_FILE_PATH).getAbsolutePath(),
                    "edc.vault", getFileFromRelativePath(CONSUMER_VAULT_PROPERTIES_FILE_PATH).getAbsolutePath()

            )
    );

    final ConsumerPullSampleTestCommon testUtils =new ConsumerPullSampleTestCommon();

    @Before
    public void startServer() throws Exception {
        var command = "HTTP_SERVER_PORT=4000 java -jar util/http-request-logger/build/libs/http-request-logger.jar";
        serverProcess = Runtime.getRuntime().exec(command);
        Thread.sleep(2000);

        Logger logger = Logger.getLogger("HTTPServerLogger");
        serverLogStream = serverProcess.getInputStream();
    }

    @After
    public void stopServer() {
        serverProcess.destroy();
    }

    private static String findAuthCode(InputStream logStream) throws IOException {
        var reader = new BufferedReader(new InputStreamReader(logStream));
        String line;
        var pattern = Pattern.compile("\"authCode\": \"(.*?)\"");
        String authCode=null;

        while ((line = reader.readLine()) != null) {
            var matcher = pattern.matcher(line);
            if (matcher.find()) {
                authCode= matcher.group(1);
                break;
            }
        }

        return authCode;
    }


    @Test
    void runSampleSteps() throws Exception {
        testUtils.registerDataPlaneInstanceProvider();
        testUtils.registerDataPlaneInstanceConsumer();
        testUtils.createAsset();
        testUtils.createPolicyDefinition();
        contractOfferId = testUtils.createContractDefinition();
        testUtils.fetchCatalog();
        contractNegotiationId = testUtils.initiateContractNegotiation();
        contractAgreementId = testUtils.getContractAgreement(contractNegotiationId);
        transferProcessId = testUtils.createTransferProcess(contractAgreementId);
        testUtils.getTransferProcess(transferProcessId);
        String authCode = findAuthCode(serverLogStream);
        testUtils.pullData(authCode);
    }




}
