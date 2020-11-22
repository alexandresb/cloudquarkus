package org.cesi.chapter07;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class OrderEndpointTest {
    @Test
    public void testOrderService(){

        JsonObject objOrder = Json.createObjectBuilder()
                .add("item", "bike")
                .add("price", new Long(100))
                .build();

        // Test qu'un user peut poster un order (ici pour customer 1)
        given()
                .auth()
                .preemptive()
                .basic("frank", "123")
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
                .basic("joe", "123")
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
                .basic("joe", "123")
                .when().get("/orders?customerId=1")
                .then()
                .statusCode(200)
                .body(containsString("mountain bike"));

        // Test qu'un admin peut supprimer un order
        given()
                .auth()
                .preemptive()
                .basic("joe", "123")
                .when().delete("/orders/1")
                .then()
                .statusCode(204);



    }
}
