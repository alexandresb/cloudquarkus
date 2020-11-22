package org.cesi.chapter09;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class CustomerEndpointTest {


    @Test
    public void testCustomerService(){
        //API JSON-B
        JsonObject obj = Json.createObjectBuilder()
                            .add("name","John'")
                            .add("surname","Smith")
                            .build();

        //utilisation de REST Assured
        //Test POST
        given()
             .contentType("application/json")
             .body(obj.toString())
                .when()
                .post("/customers")
                .then()
                  .statusCode(201);
        //Test GET
        given()
             .when().get("customers")
                .then()
                  .statusCode(200)
                  .body(containsString("John"),containsString("Smith"));

        obj = Json.createObjectBuilder()
                .add("id", 0)
                .add("name", "Donald")
                .add("surname", "Duck").build();

        //Test PUT
        given()
                .contentType("application/json")
                .body(obj.toString())
                .when()
                .put("/customers")
                .then()
                  .statusCode(204);

        //Test DELETE
        given()
               // .contentType("application/json")
                .when()
                .delete("/customers?id=0")
                .then()
                  .statusCode(204);
    }
}
