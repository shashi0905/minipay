package com.shashi.minipay.provider.service;

import com.shashi.minipay.provider.dto.ChargeRequest;
import com.shashi.minipay.provider.dto.ChargeResponse;
import com.shashi.minipay.provider.dto.RefundRequest;
import com.shashi.minipay.provider.dto.RefundResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentProviderService {

    private final Map<String, ChargeResponse> charges = new ConcurrentHashMap<>();
    private final Map<String, RefundResponse> refunds = new ConcurrentHashMap<>();
    private final Map<String, ChargeResponse> idempotencyKeys = new ConcurrentHashMap<>();

    @Value("${provider.processing-delay-ms:500}")
    private long processingDelayMs;

    @Value("${provider.failure-rate:0.1}")
    private double failureRate;

    private final Random random = new Random();

    public ChargeResponse createCharge(ChargeRequest request) throws Exception {
        Thread.sleep(processingDelayMs);

        if (idempotencyKeys.containsKey(request.idempotencyKey())) {
            return idempotencyKeys.get(request.idempotencyKey());
        }

        String chargeId = "ch_" + UUID.randomUUID().toString().replace("-", "");
        boolean shouldFail = random.nextDouble() < failureRate;

        ChargeResponse response;
        if (shouldFail) {
            response = new ChargeResponse(
                    chargeId,
                    request.orderId(),
                    request.userId(),
                    request.amount(),
                    request.currency(),
                    "failed",
                    request.paymentMethodId(),
                    Instant.now(),
                    "card_declined",
                    "Your card was declined."
            );
        } else {
            response = new ChargeResponse(
                    chargeId,
                    request.orderId(),
                    request.userId(),
                    request.amount(),
                    request.currency(),
                    "succeeded",
                    request.paymentMethodId(),
                    Instant.now(),
                    null,
                    null
            );
        }

        charges.put(chargeId, response);
        idempotencyKeys.put(request.idempotencyKey(), response);

        return response;
    }

    public ChargeResponse getCharge(String chargeId) {
        ChargeResponse charge = charges.get(chargeId);
        if (charge == null) {
            throw new IllegalArgumentException("Charge not found: " + chargeId);
        }
        return charge;
    }

    public RefundResponse createRefund(RefundRequest request) throws Exception {
        Thread.sleep(processingDelayMs);

        ChargeResponse charge = charges.get(request.chargeId());
        if (charge == null) {
            throw new IllegalArgumentException("Charge not found: " + request.chargeId());
        }

        if (!"succeeded".equals(charge.status())) {
            throw new IllegalArgumentException("Cannot refund a charge with status: " + charge.status());
        }

        String refundId = "re_" + UUID.randomUUID().toString().replace("-", "");
        RefundResponse response = new RefundResponse(
                refundId,
                request.chargeId(),
                request.amount(),
                charge.currency(),
                "succeeded",
                Instant.now()
        );

        refunds.put(refundId, response);

        return response;
    }

    public RefundResponse getRefund(String refundId) {
        RefundResponse refund = refunds.get(refundId);
        if (refund == null) {
            throw new IllegalArgumentException("Refund not found: " + refundId);
        }
        return refund;
    }

    public void reset() {
        charges.clear();
        refunds.clear();
        idempotencyKeys.clear();
    }
}
