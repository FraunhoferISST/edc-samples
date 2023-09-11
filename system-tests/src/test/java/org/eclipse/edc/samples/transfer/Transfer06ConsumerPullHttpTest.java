package org.eclipse.edc.samples.transfer;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.samples.transfer.FileTransferSampleTestCommon.getFileFromRelativePath;

@EndToEndTest
public class Transfer06ConsumerPullHttpTest {

    static final String CONSUMER_CONFIG_PROPERTIES_FILE_PATH = "transfer/transfer-06-consumer-pull-http/http-pull-consumer/consumer-configuration.properties";

    static final String PROVIDER_CONFIG_PROPERTIES_FILE_PATH = "transfer/transfer-06-consumer-pull-http/http-pull-provider/provider-configuration.properties";
    static final String SAMPLE_ASSET_FILE_PATH = "transfer/transfer-06-consumer-pull-http/README.md";

    private static String authCode="";
    private static InputStream serverLogStream=null;
    private static Logger logger=null;

    private Process serverProcess;

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

    @Before
    public void startServer() throws Exception {
        String command = "HTTP_SERVER_PORT=4000 java -jar util/http-request-logger/build/libs/http-request-logger.jar";
        serverProcess = Runtime.getRuntime().exec(command);
        Thread.sleep(2000);

        logger= Logger.getLogger("HTTPServerLogger");
        serverLogStream = serverProcess.getInputStream();
    }

    private static String findAuthCode(InputStream logStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(logStream));
        String line;
        Pattern pattern = Pattern.compile("\"authCode\": \"(.*?)\"");
        String authCode=null;

        while ((line = reader.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                authCode= matcher.group(1);
                break;
            }
        }

        return authCode;
    }

    @After
    public void stopServer() {
        serverProcess.destroy();
    }

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
        authCode=findAuthCode(serverLogStream);
        testUtils.pullData(authCode);
    }




}
