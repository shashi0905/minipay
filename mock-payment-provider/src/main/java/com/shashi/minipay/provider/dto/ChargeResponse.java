package com.shashi.minipay.provider.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ChargeResponse(
        String id,
        String orderId,
        String userId,
        BigDecimal amount,
        String currency,
        String status,
        String paymentMethodId,
        Instant createdAt,
        String failureCode,
        String failureMessage
) {
}
