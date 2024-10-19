package org.achumakin;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.achumakin.entities.PaymentRecord;
import org.achumakin.mock.DatabaseTestResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@Testcontainers
@QuarkusTestResource(DatabaseTestResource.class)
public class PaymentsTestCombine {

    private final List<Integer> paymentIds = new ArrayList<>();
    private static final Header authHeader = new Header("Authorization", "Basic am9objpqb2hu");

    @BeforeEach
    void deleteAllPayments() {
        Response allPaymentsResponse = given()
                .header(authHeader)
                .when()
                .get("/payments")
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<Map<String, Object>> allPayments = allPaymentsResponse.jsonPath().getList("$");
        if (!allPayments.isEmpty()) {
            for (Map<String, Object> payment : allPayments) {
                Integer paymentId = (Integer) payment.get("id");
                deletePaymentHelper(paymentId);
            }
        }
    }

    @AfterEach
    public void cleanup() {
        for (Integer paymentId : paymentIds) {
            deletePaymentHelper(paymentId);
        }
        paymentIds.clear();
    }

    private void deletePaymentHelper(Integer paymentId) {
        given()
                .header(authHeader)
                .when()
                .delete("/payments" + "/" + paymentId)
                .then()
                .statusCode(204);
    }

    private Integer getNewPaymentIdHelper(PaymentRecord payment) {
        return given()
                .header(authHeader)
                .contentType(ContentType.JSON)
                .body(payment)
                .when()
                .post("/payments")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    private PaymentRecord createPaymentHelper(int i) {
        PaymentRecord payment = new PaymentRecord();
        payment.setAmount(10.0 * i);
        payment.setCurrency(i % 2 == 0 ? "USD" : "EUR");
        payment.setName("Test Consumer " + i);
        return payment;
    }

    @Test
    public void testCreatePaymentAndVerify() {
        // Create a new payment
        var payment = new PaymentRecord();
        payment.setAmount(12.34);
        payment.setCurrency("EUR");
        payment.setName("Test Consumer");

        // Get the paymentId
        Integer createdPaymentId =
                given()
                        .header(authHeader)
                        .contentType(ContentType.JSON)
                        .body(payment)
                        .when()
                        .post("/payments")
                        .then()
                        .statusCode(201)
                        .extract().path("id");

        paymentIds.add(createdPaymentId);

        System.out.println(createdPaymentId);

        // Verify the payment
        given()
                .header(authHeader)
                .when()
                .get("/payments"+ "/" + createdPaymentId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdPaymentId))
                .body("amount", equalTo(12.34F))
                .body("currency", equalTo(payment.getCurrency()))
                .body("name", equalTo(payment.getName()));
    }



    @Test
    public void testCreateMultiplePaymentsAndVerify() {
        int numberOfPayments = 3;

        // Create multiple payments using a loop
        for (int i = 1; i <= numberOfPayments; i++) {
            PaymentRecord payment = createPaymentHelper(i);
            paymentIds.add(getNewPaymentIdHelper(payment));
        }

        // Use GET ALL PAYMENTS to verify
        Response allPayments = given()
                .header(authHeader)
                .when()
                .get("/payments")
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<Map<String, Object>> payments = allPayments.jsonPath().getList("$");
        System.out.println(payments);

        // Verify for each created payment
        for (int i = 0; i < numberOfPayments; i++) {
            PaymentRecord expectedPayment = createPaymentHelper(i + 1);
            Map<String, Object> actualPayment = payments.get(i);

            assertEquals(expectedPayment.getAmount().toString(), actualPayment.get("amount").toString());
            assertEquals(expectedPayment.getCurrency(), actualPayment.get("currency"));
            assertEquals(expectedPayment.getName(), actualPayment.get("name"));
        }
    }
}

