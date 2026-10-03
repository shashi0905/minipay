package com.shashi.minipay.payment.dto.response;

import com.shashi.minipay.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreatePaymentResponse(
        UUID id,
        String orderId,
        String userId,
        BigDecimal amount,
        String currency,
        String providerTransactionId,
        PaymentStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
