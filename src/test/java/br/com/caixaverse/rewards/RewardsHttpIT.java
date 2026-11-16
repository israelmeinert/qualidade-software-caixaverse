package br.com.caixaverse.rewards;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(TestJwtKeys.class)
class RewardsHttpIT {
    @Test
    void applicationExposesHealthAndSeededCatalog() {
        given().get("/q/health/live").then().statusCode(200).body("status", equalTo("UP"));
        given().get("/q/health/ready").then().statusCode(200).body("status", equalTo("UP"));
        given().get("/catalog").then().statusCode(200).body("size()", greaterThanOrEqualTo(3));
        given().queryParam("size", 1).get("/catalog").then().statusCode(200).body("size()", equalTo(1));
        given().queryParam("size", 101).get("/catalog").then().statusCode(400);
    }

    @Test
    void verifiesAuthorizationByRoleAndResource() {
        given().get("/me/wallet").then().statusCode(401)
                .header("X-Correlation-ID", matchesPattern("[a-f0-9-]{36}"))
                .body("code", equalTo("unauthenticated"));
        given().auth().oauth2("not-a-jwt").get("/me/wallet").then().statusCode(401);
        given().auth().oauth2(token("test-admin", "admin")).get("/me/wallet").then().statusCode(403);
        String owner = employeeWithNinetyPoints();
        given().auth().oauth2(token(owner, "employee")).contentType(ContentType.JSON)
                .body(productRequest("CANECAS", 40, 1, true, true)).post("/admin/products").then().statusCode(403);

        long product = product(40, 2, true);
        String order = redeem(owner, "owner", product, 1, "Ana").then().statusCode(200)
                .body("unitPrice", equalTo(40)).body("state", equalTo("CONFIRMED")).extract().path("id");
        String outsider = employeeWithNinetyPoints();
        given().auth().oauth2(token(outsider, "employee"))
                .get("/me/redemptions/{id}", order).then().statusCode(404);
        given().auth().oauth2(token(owner, "employee"))
                .get("/me/redemptions/{id}", order).then().statusCode(200).body("id", equalTo(order));
        wallet(owner).statusCode(200).body("balance", equalTo(50));
        wallet(outsider).statusCode(200).body("balance", equalTo(90));
        ledger(owner).statusCode(200).body("points", containsInAnyOrder(90, -40));
        assertEquals(1, stock(product));
    }

    @Test
    void creditIsIdempotentAcrossRequestsAndRejectsDifferentDeliveries() {
        String employee = identifier();
        int original = credit(employee, 10).statusCode(200).body("points", equalTo(150)).extract().path("id");
        credit(employee, 10).statusCode(200).body("id", equalTo(original));
        credit(employee, 11).statusCode(409).body("code", equalTo("credit_already_posted"));
        wallet(employee).statusCode(200).body("balance", equalTo(150));
        ledger(employee).statusCode(200).body("points", containsInAnyOrder(150));
    }

    @Test
    void retryKeepsOrderAndPriceAfterCatalogChange() {
        String employee = employeeWithNinetyPoints();
        long product = product(40, 1, true);
        String order = redeem(employee, "same", product, 1, "João").then().statusCode(200)
                .body("totalPoints", equalTo(40)).extract().path("id");

        String sku = given().get("/catalog/{id}", product).then().statusCode(200).extract().path("sku");
        given().auth().oauth2(token("test-admin", "admin")).contentType(ContentType.JSON)
                .body(productRequest(sku, 75, 0, false, true)).put("/admin/products/{id}", product)
                .then().statusCode(200).body("active", equalTo(false));
        given().get("/catalog/{id}", product).then().statusCode(404);
        redeem(employee, "same", product, 1, "João").then().statusCode(200)
                .body("id", equalTo(order)).body("unitPrice", equalTo(40));
        redeem(employee, "same", product, 1, "Outro").then().statusCode(409)
                .body("code", equalTo("idempotency_conflict"));
        redeem(employee, "other", product, 1, null).then().statusCode(404);
        wallet(employee).statusCode(200).body("balance", equalTo(50));
        ledger(employee).statusCode(200).body("points", containsInAnyOrder(90, -40));
    }

    @Test
    void invalidRequestsCannotChangeBalanceStockOrLedger() {
        String employee = employeeWithNinetyPoints();
        long product = product(60, 2, false);
        redeem(employee, "bad-quantity", product, 0, null).then()
                .statusCode(400).body("code", equalTo("invalid_request"));
        redeem(employee, "bad-balance", product, 2, null).then()
                .statusCode(409).body("code", equalTo("insufficient_balance"));
        redeem(employee, "bad-stock", product, 3, null).then()
                .statusCode(409).body("code", equalTo("insufficient_stock"));
        redeem(employee, "unsupported", product, 1, "Texto").then().statusCode(400);
        redeem(employee, "markup", product, 1, "<b>Ana</b>").then().statusCode(400);
        given().auth().oauth2(token(employee, "employee")).header("Idempotency-Key", "forged-price")
                .contentType(ContentType.JSON).body(Map.of("productId", product, "quantity", 1, "price", 1))
                .post("/me/redemptions").then().statusCode(400);
        given().auth().oauth2(token(employee, "employee")).contentType(ContentType.JSON)
                .body(Map.of("productId", product, "quantity", 1)).post("/me/redemptions").then().statusCode(400);
        wallet(employee).statusCode(200).body("balance", equalTo(90));
        ledger(employee).statusCode(200).body("points", containsInAnyOrder(90));
        assertEquals(2, stock(product));
    }

    @Test
    void concurrentWalletsCannotBuyTheSameLastItem() throws Exception {
        String alice = employeeWithNinetyPoints();
        String bruno = employeeWithNinetyPoints();
        long product = product(40, 1, false);

        var replies = concurrently(() -> redeem(alice, "stock", product, 1, null),
                () -> redeem(bruno, "stock", product, 1, null));

        assertOneWinner(replies, "insufficient_stock");
        assertEquals(0, stock(product));
        assertEquals(List.of(50, 90), List.of(balance(alice), balance(bruno)).stream().sorted().toList());
    }

    @Test
    void concurrentOrdersCannotSpendTheSameBalanceOnDifferentProducts() throws Exception {
        String employee = employeeWithNinetyPoints();
        long first = product(60, 1, false);
        long second = product(60, 1, false);

        var replies = concurrently(() -> redeem(employee, "one", first, 1, null),
                () -> redeem(employee, "two", second, 1, null));

        assertOneWinner(replies, "insufficient_balance");
        assertEquals(30, balance(employee));
        assertEquals(1, stock(first) + stock(second));
        ledger(employee).statusCode(200).body("points", containsInAnyOrder(90, -60));
    }

    @Test
    void concurrentRetriesReturnTheSameOrder() throws Exception {
        String employee = employeeWithNinetyPoints();
        long product = product(40, 2, true);
        Supplier<Response> request = () -> redeem(employee, "shared", product, 1, "Ana");
        var replies = concurrently(request, request);

        replies.forEach(reply -> reply.then().statusCode(200));
        String firstOrder = replies.get(0).path("id");
        String secondOrder = replies.get(1).path("id");
        assertEquals(firstOrder, secondOrder);
        assertEquals(1, stock(product));
        wallet(employee).body("balance", equalTo(50));
        ledger(employee).body("points", containsInAnyOrder(90, -40));
    }

    private static List<Response> concurrently(Supplier<Response> left, Supplier<Response> right) throws Exception {
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = executor.submit(() -> startTogether(left, ready, start));
            var second = executor.submit(() -> startTogether(right, ready, start));
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            return List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
        }
    }

    private static Response startTogether(Supplier<Response> request, CountDownLatch ready, CountDownLatch start)
            throws InterruptedException {
        ready.countDown();
        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("A outra chamada concorrente não iniciou");
        }
        return request.get();
    }

    private static void assertOneWinner(List<Response> replies, String error) {
        assertEquals(List.of(200, 409), replies.stream().map(Response::statusCode).sorted().toList());
        replies.stream().filter(reply -> reply.statusCode() == 409).findFirst().orElseThrow()
                .then().body("code", equalTo(error));
    }

    private static String identifier() {
        return UUID.randomUUID().toString();
    }

    private static String employeeWithNinetyPoints() {
        String employee = identifier();
        credit(employee, 9).statusCode(200).body("points", equalTo(90));
        return employee;
    }

    private static String token(String employee, String role) {
        return TestJwtKeys.token(employee, role);
    }

    private static Map<String, Object> productRequest(String sku, int price, int stock, boolean active,
                                                      boolean customizable) {
        return Map.of("sku", sku, "description", "Produto de teste", "price", price, "stock", stock,
                "active", active, "customizable", customizable);
    }

    private static long product(int price, int stock, boolean customizable) {
        return given().auth().oauth2(token("test-admin", "admin")).contentType(ContentType.JSON)
                .body(productRequest(identifier().toUpperCase(Locale.ROOT), price, stock, true, customizable))
                .post("/admin/products").then().statusCode(201).extract().jsonPath().getLong("id");
    }

    private static int stock(long product) {
        return given().get("/catalog/{id}", product).then().statusCode(200).extract().path("stock");
    }

    private static int balance(String employee) {
        return wallet(employee).statusCode(200).extract().path("balance");
    }

    private static ValidatableResponse credit(String employee, int deliveries) {
        return given().auth().oauth2(token("test-admin", "admin")).contentType(ContentType.JSON)
                .body(Map.of("competence", "2030-06", "deliveries", deliveries))
                .put("/admin/employees/{id}/credits", employee).then();
    }

    private static ValidatableResponse wallet(String employee) {
        return given().auth().oauth2(token(employee, "employee")).get("/me/wallet").then();
    }

    private static ValidatableResponse ledger(String employee) {
        return given().auth().oauth2(token(employee, "employee")).get("/me/ledger").then();
    }

    private static Response redeem(String employee, String key, long product, int quantity, String personalization) {
        var request = new HashMap<String, Object>(Map.of("productId", product, "quantity", quantity));
        if (personalization != null) {
            request.put("personalization", personalization);
        }
        return given().auth().oauth2(token(employee, "employee"))
                .header("Idempotency-Key", key).contentType(ContentType.JSON).body(request).post("/me/redemptions");
    }
}
