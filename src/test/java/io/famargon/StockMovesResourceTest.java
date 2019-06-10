package io.famargon;

import static org.hamcrest.CoreMatchers.is;

import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.vertx.core.json.JsonObject;

@QuarkusTest
public class StockMovesResourceTest {

    @Test
    public void testStocksEndpoint() {
        RestAssured.given()
          .when().get("/stocks")
          .then()
             .statusCode(200)
             .body(is("[]"));
    }

    @Disabled
    @Test
    public void testSendStockMove() {
        StockMove move = new StockMove();
        move.setItem("itemA");
        move.setQuantity(1);
        RestAssured.given()
        .when()
            .body(JsonObject.mapFrom(move).encode())
            .post("/stockmoves")
        .then()
            .statusCode(200);
        
        RestAssured.given()
          .when().get("/stocks")
          .then()
             .statusCode(200)
             .body(is(JsonObject.mapFrom(Map.of("itemA", 1)).encode()));

    }

}