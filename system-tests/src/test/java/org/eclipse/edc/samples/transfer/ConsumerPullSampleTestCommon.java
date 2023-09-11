package org.eclipse.edc.samples.transfer;

import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.apache.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ConsumerPullSampleTestCommon {

    static final String MANAGEMENT_API_URL = "http://localhost:19193";
    String ContractOfferId;
    String ContractNegotiationId;
    String ContractAgreementId;
    String TransferProcessId;

    public ConsumerPullSampleTestCommon() {
    }

    public void createAsset(){
        String requestBody = "{\n" +
                "    \"@context\": {\n" +
                "        \"edc\": \"https://w3id.org/edc/v0.0.1/ns/\",\n" +
                "        \"odrl\": \"http://www.w3.org/ns/odrl/2/\"\n" +
                "    },\n" +
                "    \"@id\": \"aPolicy\",\n" +
                "    \"policy\": {\n" +
                "        \"@type\": \"set\",\n" +
                "        \"odrl:permission\": [],\n" +
                "        \"odrl:prohibition\": [],\n" +
                "        \"odrl:obligation\": []\n" +
                "    }\n" +
                "}";

        given().headers("Content-Type","application/json")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(MANAGEMENT_API_URL+"/management/v2/assets")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("@id", not(emptyString()));
    }

    public void createPolicyDefinition() {
        String requestBody = "{\n" +
                "    \"@context\": {\n" +
                "        \"edc\": \"https://w3id.org/edc/v0.0.1/ns/\",\n" +
                "        \"odrl\": \"http://www.w3.org/ns/odrl/2/\"\n" +
                "    },\n" +
                "    \"@id\": \"aPolicy\",\n" +
                "    \"policy\": {\n" +
                "        \"@type\": \"set\",\n" +
                "        \"odrl:permission\": [],\n" +
                "        \"odrl:prohibition\": [],\n" +
                "        \"odrl:obligation\": []\n" +
                "    }\n" +
                "}";

        given()
                .header("Content-Type", "application/json")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(MANAGEMENT_API_URL + "/management/v2/policydefinitions")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("@id", not(emptyString()));
    }

    public void createContractDefinition(){
        String requestBody = "{\n" +
                "    \"@context\": {\n" +
                "        \"edc\": \"https://w3id.org/edc/v0.0.1/ns/\"\n" +
                "    },\n" +
                "    \"@id\": \"1\",\n" +
                "    \"accessPolicyId\": \"aPolicy\",\n" +
                "    \"contractPolicyId\": \"aPolicy\",\n" +
                "    \"assetsSelector\": []\n" +
                "}";

        ContractOfferId=given()
                .header("Content-Type", "application/json")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(MANAGEMENT_API_URL + "/management/v2/contractdefinitions")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("@id", not(emptyString()))
                .extract()
                .jsonPath()
                .get("@id");
    }

    public void fetchCatalog(){
        String requestBody = "{\n" +
                "    \"@context\": {\n" +
                "        \"edc\": \"https://w3id.org/edc/v0.0.1/ns/\"\n" +
                "    },\n" +
                "    \"providerUrl\": \"http://localhost:19194/protocol\",\n" +
                "    \"protocol\": \"dataspace-protocol-http\"\n" +
                "}";

        given()
                .header("Content-Type", "application/json")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("http://localhost:29193/management/v2/catalog/request")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("@id",not(emptyString()))
                .body("@type",equalTo("dcat:Catalog"));
    }

    public void initiateContractNegotiation(){
        String requestBody = "{\n" +
                "    \"@context\": {\n" +
                "        \"edc\": \"https://w3id.org/edc/v0.0.1/ns/\",\n" +
                "        \"odrl\": \"http://www.w3.org/ns/odrl/2/\"\n" +
                "    },\n" +
                "    \"@type\": \"NegotiationInitiateRequestDto\",\n" +
                "    \"connectorId\": \"provider\",\n" +
                "    \"connectorAddress\": \"http://localhost:19194/protocol\",\n" +
                "    \"consumerId\": \"consumer\",\n" +
                "    \"providerId\": \"provider\",\n" +
                "    \"protocol\": \"dataspace-protocol-http\",\n" +
                "    \"offer\": {\n" +
                "        \"offerId\": \"MQ==:YXNzZXRJZA==:YTc4OGEwYjMtODRlZi00NWYwLTgwOWQtMGZjZTMwMGM3Y2Ey\",\n" +
                "        \"assetId\": \"assetId\",\n" +
                "        \"policy\": {\n" +
                "            \"@id\": \"MQ==:YXNzZXRJZA==:YTc4OGEwYjMtODRlZi00NWYwLTgwOWQtMGZjZTMwMGM3Y2Ey\",\n" +
                "            \"@type\": \"Set\",\n" +
                "            \"odrl:permission\": [],\n" +
                "            \"odrl:prohibition\": [],\n" +
                "            \"odrl:obligation\": [],\n" +
                "            \"odrl:target\": \"assetId\"\n" +
                "        }\n" +
                "    }\n" +
                "}";

        ContractNegotiationId=given()
                .header("Content-Type", "application/json")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("http://localhost:29193/management/v2/contractnegotiations")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("@id",not(emptyString()))
                .extract()
                .jsonPath()
                .get("@id");


    }

    public void getContractAgreement(String negotiationId) {
        ContractAgreementId = given()
                .header("Content-Type", "application/json")
                .when()
                .get(MANAGEMENT_API_URL + "/management/v2/contractnegotiations/" + negotiationId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("edc:state",equalTo("FINALIZED"))
                .extract()
                .jsonPath()
                .get("@id");
    }

    public void createTransferProcess(String contractAgreementId) {
        String requestBody = "{\n" +
                "    \"@context\": {\n" +
                "        \"edc\": \"https://w3id.org/edc/v0.0.1/ns/\"\n" +
                "    },\n" +
                "    \"@type\": \"TransferRequestDto\",\n" +
                "    \"connectorId\": \"provider\",\n" +
                "    \"connectorAddress\": \"http://localhost:19194/protocol\",\n" +
                "    \"contractId\": \"" + contractAgreementId + "\",\n" +
                "    \"assetId\": \"assetid\",\n" +
                "    \"managedResources\": false,\n" +
                "    \"protocol\": \"dataspace-protocol-http\",\n" +
                "    \"dataDestination\": {\n" +
                "        \"type\": \"HttpProxy\"\n" +
                "    }\n" +
                "}";

        TransferProcessId=given()
                .header("Content-Type", "application/json")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("http://localhost:29193/management/v2/transferprocesses")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("@id",not(emptyString()))
                .extract()
                .jsonPath()
                .get("@id");
    }

    public void getTransferProcess(String transferProcessId) {
        given()
                .when()
                .get(MANAGEMENT_API_URL + "/management/v2/transferprocesses/" + transferProcessId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("edc:state", equalTo("COMPLETED"));
    }

    public void pullData(String authCode) {
        given()
                .header("Authorization",authCode)
                .when()
                .get("http://localhost:29291/public/1")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name",equalTo("Leanne Graham"));
    }
}
