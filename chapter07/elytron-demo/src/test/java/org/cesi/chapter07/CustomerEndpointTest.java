package org.cesi.chapter07;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class CustomerEndpointTest {


    @Test
    public void testCustomerService(){
        //Test GET (2 Customers obtenus)
        //test si un rôle user peut lister les Customers
        given()
                .auth()
                .preemptive()
                .basic("frank", "123")
                .when().get("/customers")
                .then()
                .statusCode(200)
                .body("$.size()", is(2));

        //test qu'un admin est autorisé à lister les Customers (joe a le rôle admin)
        given()
                .auth()
                .preemptive()
                .basic("joe", "123")
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
                .basic("frank", "123")
                .contentType("application/json")
                .body(objCustomer.toString())
                .when().post("/customers")
                .then()
                .statusCode(201);

        //test qu'un admin peut retrouver un customer en fonction de son id (ici 3)
        given()
                .auth()
                .preemptive()
                .basic("joe", "123")
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
                .basic("joe", "123")
                .contentType("application/json")
                .body(objCustomer.toString())
                .when().put("/customers")
                .then()
                .statusCode(204);

        //test qu'un user n'est pas autorisé à supprimer un Customer (ici cust 3) - que le code http 403 (forbiddent) est retourné
        given()
                .auth()
                .preemptive()
                .basic("frank", "123")
                .queryParam("id",3)
                .when().delete("/customers")
                .then()
                .statusCode(403);

       //test qu'un admin EST autorisé à supprimer un Customer (ici cust 3) - code 204 retourné
        given()
                .auth()
                .preemptive()
                .basic("joe", "123")
                .queryParam("id",3)
                .when().delete("/customers")
                .then()
                .statusCode(204);


    }
}
