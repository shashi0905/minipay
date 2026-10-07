package com.shashi.minipay.payment.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

public record ProviderRefundResponse(
        @JsonProperty("id")
        String id,

        @JsonProperty("chargeId")
        String chargeId,

        @JsonProperty("amount")
        BigDecimal amount,

        @JsonProperty("currency")
        String currency,

        @JsonProperty("status")
        String status,

        @JsonProperty("createdAt")
        Instant createdAt
) {
}
