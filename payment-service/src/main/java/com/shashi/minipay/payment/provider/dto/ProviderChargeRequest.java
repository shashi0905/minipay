package com.shashi.minipay.payment.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ProviderChargeRequest(
        @JsonProperty("orderId")
        String orderId,

        @JsonProperty("userId")
        String userId,

        @JsonProperty("amount")
        BigDecimal amount,

        @JsonProperty("currency")
        String currency,

        @JsonProperty("paymentMethodId")
        String paymentMethodId,

        @JsonProperty("idempotencyKey")
        String idempotencyKey
) {
}
