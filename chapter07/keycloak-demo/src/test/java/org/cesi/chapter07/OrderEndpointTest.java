package org.cesi.chapter07;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import java.io.StringReader;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class OrderEndpointTest {
    //injection de l'URL de base du provider keycloak définie dans application.properties
    @ConfigProperty(name = "keycloak.url")
    String keycloakURL;
    @Test
    public void testOrderService(){
        //obtention d'un access token correspondant à un utilisateur ayant le rôle user
        RestAssured.baseURI = keycloakURL;//assignation de l'uri de base
        Response response = given().urlEncodingEnabled(true)
                .auth().preemptive().basic("customer-service","mysecret")//authen de l'app cliente
                .param("grant_type", "password")//authentification de l'utilisateur via password
                .param("client_id", "customer-service")//id app cliente
                .param("username", "frank")//rôle user
                .param("password", "test")//password de frank
                .header("Accept", ContentType.JSON.getAcceptHeader())//post de username (login) et du pwd à l'adresse
                .post("/auth/realms/quarkus-realm/protocol/openid-connect/token")
                .then().statusCode(200).extract()
                .response();
        //récupération de l'access token retourné dans la response
        JsonReader jsonReader = Json.createReader(new StringReader(response.getBody().asString()));
        JsonObject object = jsonReader.readObject();
        String userToken = object.getString("access_token");

        //récupération du token pour un utilisateur ayant le rôle admin
        response = given().urlEncodingEnabled(true)
                .auth().preemptive().basic("customer-service","mysecret")//authen de l'app cliente
                .param("grant_type", "password")//authentification de l'utilisateur via password
                .param("client_id", "customer-service")//id app cliente
                .param("username", "joe")//rôle admin (et user)
                .param("password", "test")//password de joe
                .header("Accept", ContentType.JSON.getAcceptHeader())//post de username (login) et du pwd à l'adresse
                .post("/auth/realms/quarkus-realm/protocol/openid-connect/token")
                .then().statusCode(200).extract()
                .response();
        //récupération de l'access token "admin" retourné dans la response
        jsonReader = Json.createReader(new StringReader(response.getBody().asString()));
        object = jsonReader.readObject();
        String adminToken = object.getString("access_token");

        RestAssured.baseURI = "http://localhost:8081";//assignation de l'URL de base de l'application keycloak-demo pour les tests (port / défaut 8081)

        JsonObject objOrder = Json.createObjectBuilder()
                .add("item", "bike")
                .add("price", new Long(100))
                .build();

        // Test qu'un user peut poster un order (ici pour customer 1)
        given()
                .auth()
                .preemptive()
               .oauth2(userToken)
                .contentType("application/json")
                .body(objOrder.toString())
                .when()
                .post("/orders/1")
                .then()
                .statusCode(201);

        // Create new JSON for Order #1
        objOrder = Json.createObjectBuilder()
                .add("id", new Long(1))
                .add("item", "mountain bike")
                .add("price", new Long(100))
                .build();

        // Test qu'un admin peut modifier un order
        given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .contentType("application/json")
                .body(objOrder.toString())
                .when()
                .put("/orders")
                .then()
                .statusCode(204);

        // Test qu'un admin peut lister les orders d'un customer donné (ici cust 1)
        given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .when().get("/orders?customerId=1")
                .then()
                .statusCode(200)
                .body(containsString("mountain bike"));

        // Test qu'un admin peut supprimer un order
        given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .when().delete("/orders/1")
                .then()
                .statusCode(204);

    }
}
