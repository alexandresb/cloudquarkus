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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class CustomerEndpointTest {
    //injection de l'URL de base du provider keycloak définie dans application.properties
    @ConfigProperty(name = "keycloak.url")
    String keycloakURL;

    @Test
    public void testCustomerService(){
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
        //test si un rôle user peut lister les Customers
        given()
                .auth()
                .preemptive()
                .oauth2(userToken)
                .when().get("/customers")
                .then()
                .statusCode(200)
                .body("$.size()", is(2));

        //test qu'un admin est autorisé à lister les Customers
        given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .when().get("/customers")
                .then()
                .statusCode(200)
                .body("$.size()", is(2));

        //tests qu'un user peut créer un customer
        //création d'une représentation JSON d'un nouveau Customer
        JsonObject objCustomer = Json.createObjectBuilder()
                .add("name", "Alex")
                .add("surname", "Sbriglio")
                .build();

        given()
                .auth()
                .preemptive()
                .oauth2(userToken)
                .contentType("application/json")
                .body(objCustomer.toString())
                .when().post("/customers")
                .then()
                .statusCode(201);

        //test qu'un admin peut retrouver un customer en fonction de son id (ici 3)
        given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .pathParam("custId",3)
                .when().get("/customers/{custId}")
                .then()
                .statusCode(200)
                .body(containsString("Alex"));

        //test qu'un admin est autorisé à modifier un customer
        //création de la représentation JSON du customer à modifier
        objCustomer =  Json.createObjectBuilder()
                .add("id",3)
                .add("name", "Alexandre")
                .add("surname", "Sbriglio")
                .build();

        given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .contentType("application/json")
                .body(objCustomer.toString())
                .when().put("/customers")
                .then()
                .statusCode(204);

        //test qu'un user n'est pas autorisé à supprimer un Customer (ici cust 3) - que le code http 403 (forbiddent) est retourné
        given()
                .auth()
                .preemptive()
                .oauth2(userToken)
                .queryParam("id",3)
                .when().delete("/customers")
                .then()
                .statusCode(403);

       //test qu'un admin EST autorisé à supprimer un Customer (ici cust 3) - code 204 retourné
        given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .queryParam("id",3)
                .when().delete("/customers")
                .then()
                .statusCode(204);

    }
}
