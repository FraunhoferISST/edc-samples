package org.eclipse.edc.samples.transfer;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class ConsumerPullSampleTestCommon {

    static final String MANAGEMENT_API_URL = "http://localhost:19193";

    public ConsumerPullSampleTestCommon() {
    }

    public void registerDataPlaneInstanceProvider() {
        var requestBody = """
                {
                    "@context": {
                        "edc": "https://w3id.org/edc/v0.0.1/ns/"
                    },
                    "@id": "http-pull-provider-dataplane",
                    "url": "http://localhost:19192/control/transfer",
                    "allowedSourceTypes": ["HttpData"],
                    "allowedDestTypes": ["HttpProxy", "HttpData"],
                    "properties": {
                        "https://w3id.org/edc/v0.0.1/ns/publicApiUrl": "http://localhost:19291/public/"
                    }
                }""";

        given()
                .header("Content-Type", "application/json")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(MANAGEMENT_API_URL + "/management/v2/dataplanes")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    public void registerDataPlaneInstanceConsumer() {
        var requestBody = """
                {
                    "@context": {
                        "edc": "https://w3id.org/edc/v0.0.1/ns/"
                    },
                    "@id": "http-pull-consumer-dataplane",
                    "url": "http://localhost:29192/control/transfer",
                    "allowedSourceTypes": ["HttpData"],
                    "allowedDestTypes": ["HttpProxy", "HttpData"],
                    "properties": {
                        "https://w3id.org/edc/v0.0.1/ns/publicApiUrl/publicApiUrl": "http://localhost:29291/public/"
                    }
                }""";

        given()
                .header("Content-Type", "application/json")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("http://localhost:29193/management/v2/dataplanes")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    public void createAsset() {
        var requestBody = """
                {
                    "@context": {
                        "edc": "https://w3id.org/edc/v0.0.1/ns/",
                        "odrl": "http://www.w3.org/ns/odrl/2/"
                    },
                    "@id": "aPolicy",
                    "policy": {
                        "@type": "set",
                        "odrl:permission": [],
                        "odrl:prohibition": [],
                        "odrl:obligation": []
                    }
                }""";

        given().headers("Content-Type", "application/json")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(MANAGEMENT_API_URL + "/management/v2/assets")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("@id", not(emptyString()));
    }

    public void createPolicyDefinition() {
        var requestBody = """
                {
                    "@context": {
                        "edc": "https://w3id.org/edc/v0.0.1/ns/",
                        "odrl": "http://www.w3.org/ns/odrl/2/"
                    },
                    "@id": "aPolicy",
                    "policy": {
                        "@type": "set",
                        "odrl:permission": [],
                        "odrl:prohibition": [],
                        "odrl:obligation": []
                    }
                }""";

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

    public String createContractDefinition() {
        var requestBody = """
                {
                    "@context": {
                        "edc": "https://w3id.org/edc/v0.0.1/ns/"
                    },
                    "@id": "1",
                    "accessPolicyId": "aPolicy",
                    "contractPolicyId": "aPolicy",
                    "assetsSelector": []
                }""";

        return given()
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

    public void fetchCatalog() {
        var requestBody = """
                {
                    "@context": {
                        "edc": "https://w3id.org/edc/v0.0.1/ns/"
                    },
                    "providerUrl": "http://localhost:19194/protocol",
                    "protocol": "dataspace-protocol-http"
                }""";

        given()
                .header("Content-Type", "application/json")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("http://localhost:29193/management/v2/catalog/request")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("@id", not(emptyString()))
                .body("@type", equalTo("dcat:Catalog"));
    }

    public String initiateContractNegotiation() {
        var requestBody = """
                {
                    "@context": {
                        "edc": "https://w3id.org/edc/v0.0.1/ns/",
                        "odrl": "http://www.w3.org/ns/odrl/2/"
                    },
                    "@type": "NegotiationInitiateRequestDto",
                    "connectorId": "provider",
                    "connectorAddress": "http://localhost:19194/protocol",
                    "consumerId": "consumer",
                    "providerId": "provider",
                    "protocol": "dataspace-protocol-http",
                    "offer": {
                        "offerId": "MQ==:YXNzZXRJZA==:YTc4OGEwYjMtODRlZi00NWYwLTgwOWQtMGZjZTMwMGM3Y2Ey",
                        "assetId": "assetId",
                        "policy": {
                            "@id": "MQ==:YXNzZXRJZA==:YTc4OGEwYjMtODRlZi00NWYwLTgwOWQtMGZjZTMwMGM3Y2Ey",
                            "@type": "Set",
                            "odrl:permission": [],
                            "odrl:prohibition": [],
                            "odrl:obligation": [],
                            "odrl:target": "assetId"
                        }
                    }
                }""";

        return given()
                .header("Content-Type", "application/json")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("http://localhost:29193/management/v2/contractnegotiations")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("@id", not(emptyString()))
                .extract()
                .jsonPath()
                .get("@id");


    }

    public String getContractAgreement(String negotiationId) {
        return given()
                .header("Content-Type", "application/json")
                .when()
                .get(MANAGEMENT_API_URL + "/management/v2/contractnegotiations/" + negotiationId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("edc:state", equalTo("FINALIZED"))
                .extract()
                .jsonPath()
                .get("@id");
    }

    public String createTransferProcess(String contractAgreementId) {
        var requestBody = "{\n" +
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

        return given()
                .header("Content-Type", "application/json")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("http://localhost:29193/management/v2/transferprocesses")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("@id", not(emptyString()))
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
                .header("Authorization", authCode)
                .when()
                .get("http://localhost:29291/public/1")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("Leanne Graham"));
    }
}
