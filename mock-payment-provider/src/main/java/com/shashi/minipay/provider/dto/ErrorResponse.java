package com.shashi.minipay.provider.dto;

public record ErrorResponse(
        String error,
        String message,
        String type
) {
}
