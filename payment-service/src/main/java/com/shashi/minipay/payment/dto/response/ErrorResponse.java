package com.shashi.minipay.payment.dto.response;

import java.time.Instant;

public record ErrorResponse(
        String status,
        int code,
        String message,
        String details,
        Instant timestamp
) {
}
