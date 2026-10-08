package com.shashi.minipay.provider.service;

import com.shashi.minipay.provider.dto.ChargeRequest;
import com.shashi.minipay.provider.dto.ChargeResponse;
import com.shashi.minipay.provider.dto.RefundRequest;
import com.shashi.minipay.provider.dto.RefundResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "provider.processing-delay-ms=10",
        "provider.failure-rate=0.0"
})
class PaymentProviderServiceTest {

    @Autowired
    private PaymentProviderService paymentProviderService;

    @BeforeEach
    void setUp() {
        paymentProviderService.reset();
    }

    @Test
    void createCharge_Success() throws Exception {
        ChargeRequest request = new ChargeRequest(
                "order_123",
                "user_456",
                new BigDecimal("100.00"),
                "USD",
                "pm_card_visa",
                "idempotency_key_1"
        );

        ChargeResponse response = paymentProviderService.createCharge(request);

        assertNotNull(response);
        assertEquals("order_123", response.orderId());
        assertEquals("user_456", response.userId());
        assertEquals(new BigDecimal("100.00"), response.amount());
        assertEquals("USD", response.currency());
        assertEquals("succeeded", response.status());
        assertEquals("pm_card_visa", response.paymentMethodId());
        assertNotNull(response.id());
        assertNotNull(response.createdAt());
        assertNull(response.failureCode());
        assertNull(response.failureMessage());
    }

    @Test
    void createCharge_Idempotency() throws Exception {
        ChargeRequest request = new ChargeRequest(
                "order_123",
                "user_456",
                new BigDecimal("100.00"),
                "USD",
                "pm_card_visa",
                "idempotency_key_1"
        );

        ChargeResponse response1 = paymentProviderService.createCharge(request);
        ChargeResponse response2 = paymentProviderService.createCharge(request);

        assertEquals(response1.id(), response2.id());
        assertEquals(response1.orderId(), response2.orderId());
    }

    @Test
    void getCharge_Success() throws Exception {
        ChargeRequest request = new ChargeRequest(
                "order_123",
                "user_456",
                new BigDecimal("100.00"),
                "USD",
                "pm_card_visa",
                "idempotency_key_1"
        );

        ChargeResponse created = paymentProviderService.createCharge(request);
        ChargeResponse retrieved = paymentProviderService.getCharge(created.id());

        assertEquals(created.id(), retrieved.id());
        assertEquals(created.orderId(), retrieved.orderId());
    }

    @Test
    void getCharge_NotFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentProviderService.getCharge("non_existent_charge");
        });
    }

    @Test
    void createRefund_Success() throws Exception {
        ChargeRequest chargeRequest = new ChargeRequest(
                "order_123",
                "user_456",
                new BigDecimal("100.00"),
                "USD",
                "pm_card_visa",
                "idempotency_key_1"
        );

        ChargeResponse charge = paymentProviderService.createCharge(chargeRequest);

        RefundRequest refundRequest = new RefundRequest(
                charge.id(),
                new BigDecimal("50.00"),
                "Customer requested refund"
        );

        RefundResponse refund = paymentProviderService.createRefund(refundRequest);

        assertNotNull(refund);
        assertEquals(charge.id(), refund.chargeId());
        assertEquals(new BigDecimal("50.00"), refund.amount());
        assertEquals("USD", refund.currency());
        assertEquals("succeeded", refund.status());
        assertNotNull(refund.id());
        assertNotNull(refund.createdAt());
    }

    @Test
    void createRefund_ChargeNotFound() {
        RefundRequest refundRequest = new RefundRequest(
                "non_existent_charge",
                new BigDecimal("50.00"),
                "Customer requested refund"
        );

        assertThrows(IllegalArgumentException.class, () -> {
            paymentProviderService.createRefund(refundRequest);
        });
    }

    @Test
    void getRefund_Success() throws Exception {
        ChargeRequest chargeRequest = new ChargeRequest(
                "order_123",
                "user_456",
                new BigDecimal("100.00"),
                "USD",
                "pm_card_visa",
                "idempotency_key_1"
        );

        ChargeResponse charge = paymentProviderService.createCharge(chargeRequest);

        RefundRequest refundRequest = new RefundRequest(
                charge.id(),
                new BigDecimal("50.00"),
                "Customer requested refund"
        );

        RefundResponse created = paymentProviderService.createRefund(refundRequest);
        RefundResponse retrieved = paymentProviderService.getRefund(created.id());

        assertEquals(created.id(), retrieved.id());
        assertEquals(created.chargeId(), retrieved.chargeId());
    }

    @Test
    void getRefund_NotFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentProviderService.getRefund("non_existent_refund");
        });
    }

    @Test
    void reset_ClearsAllData() throws Exception {
        ChargeRequest chargeRequest = new ChargeRequest(
                "order_123",
                "user_456",
                new BigDecimal("100.00"),
                "USD",
                "pm_card_visa",
                "idempotency_key_1"
        );

        paymentProviderService.createCharge(chargeRequest);
        paymentProviderService.reset();

        assertThrows(IllegalArgumentException.class, () -> {
            paymentProviderService.getCharge("any_charge");
        });
    }
}
