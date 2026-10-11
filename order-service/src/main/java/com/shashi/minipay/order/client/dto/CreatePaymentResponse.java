package com.shashi.minipay.order.client.dto;

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
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
