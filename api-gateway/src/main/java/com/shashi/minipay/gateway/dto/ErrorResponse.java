package com.shashi.minipay.gateway.dto;

import java.time.Instant;

/**
 * Error Response DTO - consistent error format across gateway.
 * Matches Payment Service error response format.
 */
public record ErrorResponse(
        String status,
        int code,
        String message,
        String details,
        Instant timestamp
) {
}
