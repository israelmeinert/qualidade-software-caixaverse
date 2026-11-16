package br.com.caixaverse.rewards;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.matchesPattern;

@QuarkusTest
@QuarkusTestResource(TestJwtKeys.class)
class RewardsHttpIT {
    @Test
    void applicationExposesHealthAndSeededCatalog() {
        given().get("/q/health/live").then().statusCode(200).body("status", equalTo("UP"));
        given().get("/q/health/ready").then().statusCode(200).body("status", equalTo("UP"));
        given().get("/catalog").then().statusCode(200).body("size()", greaterThanOrEqualTo(3));
    }

    @Test
    void commonCreditAndRedemptionFlow() {
        String employee = identifier();
        long product = product(40, 2, true);
        credit(employee, 9).statusCode(200).body("points", equalTo(90));
        String order = redeem(employee, "regular", product, 1, "Ana").statusCode(200)
                .body("unitPrice", equalTo(40)).body("state", equalTo("CONFIRMED")).extract().path("id");
        wallet(employee).statusCode(200).body("balance", equalTo(50));
        given().auth().oauth2(TestJwtKeys.token(employee, "employee"))
                .get("/me/redemptions/{id}", order).then().statusCode(200).body("id", equalTo(order));
        given().get("/catalog/{id}", product).then().statusCode(200).body("stock", equalTo(1));
    }

    @Test
    void rejectsAnonymousAccess() {
        given().get("/me/wallet").then().statusCode(401)
                .header("X-Correlation-ID", matchesPattern("[a-f0-9-]{36}"))
                .body("code", equalTo("unauthenticated"));
    }

    private static String identifier() {
        return UUID.randomUUID().toString();
    }

    private static long product(int price, int stock, boolean customizable) {
        var request = Map.of("sku", identifier().toUpperCase(java.util.Locale.ROOT),
                "description", "Produto de teste", "price", price, "stock", stock,
                "active", true, "customizable", customizable);
        return given().auth().oauth2(TestJwtKeys.token("test-admin", "admin"))
                .contentType(ContentType.JSON).body(request).post("/admin/products")
                .then().statusCode(201).extract().jsonPath().getLong("id");
    }

    private static ValidatableResponse credit(String employee, int deliveries) {
        return given().auth().oauth2(TestJwtKeys.token("test-admin", "admin"))
                .contentType(ContentType.JSON).body(Map.of("competence", "2030-06", "deliveries", deliveries))
                .put("/admin/employees/{id}/credits", employee).then();
    }

    private static ValidatableResponse wallet(String employee) {
        return given().auth().oauth2(TestJwtKeys.token(employee, "employee")).get("/me/wallet").then();
    }

    private static ValidatableResponse redeem(String employee, String key, long product,
                                               int quantity, String personalization) {
        var request = new java.util.HashMap<String, Object>(Map.of("productId", product, "quantity", quantity));
        if (personalization != null) {
            request.put("personalization", personalization);
        }
        return given().auth().oauth2(TestJwtKeys.token(employee, "employee"))
                .header("Idempotency-Key", key).contentType(ContentType.JSON).body(request)
                .post("/me/redemptions").then();
    }
}
