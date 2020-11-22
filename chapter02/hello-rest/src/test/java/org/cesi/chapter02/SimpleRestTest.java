package org.cesi.chapter02;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;

@QuarkusTest //permet de faciliter les TU en autorisant notamment l'injection
public class SimpleRestTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/helloworld")
          .then()
             .statusCode(200)
                //la réponse doit être retournée en moins d'1 sec
             .time(lessThan(1000L))
             .body(is("hello World"))
              .and()
                .header("Content-Length","11");
    }

    @Test
    public void testHelloEnpointWithPathParam(){
        //initialisation de la requête
        given()
          .pathParams("name","alex")
                //exe de la requête et récup réponse
          .when().get("/helloworld/{name}")
                //attentes concernant une réponse valide
            .then()
                .statusCode(200)
                .body(is("hello alex"));
    }

}