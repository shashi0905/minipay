package com.shashi.minipay.payment.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProviderErrorResponse(
        @JsonProperty("error")
        String error,

        @JsonProperty("message")
        String message,

        @JsonProperty("type")
        String type
) {
}
