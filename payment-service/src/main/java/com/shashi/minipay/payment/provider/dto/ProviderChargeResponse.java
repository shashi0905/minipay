package com.shashi.minipay.payment.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

public record ProviderChargeResponse(
        @JsonProperty("id")
        String id,

        @JsonProperty("orderId")
        String orderId,

        @JsonProperty("userId")
        String userId,

        @JsonProperty("amount")
        BigDecimal amount,

        @JsonProperty("currency")
        String currency,

        @JsonProperty("status")
        String status,

        @JsonProperty("paymentMethodId")
        String paymentMethodId,

        @JsonProperty("createdAt")
        Instant createdAt,

        @JsonProperty("failureCode")
        String failureCode,

        @JsonProperty("failureMessage")
        String failureMessage
) {
}
