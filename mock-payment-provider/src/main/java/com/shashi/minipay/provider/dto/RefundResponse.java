package com.shashi.minipay.provider.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RefundResponse(
        String id,
        String chargeId,
        BigDecimal amount,
        String currency,
        String status,
        Instant createdAt
) {
}
