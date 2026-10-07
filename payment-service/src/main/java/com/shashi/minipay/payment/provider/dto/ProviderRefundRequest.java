package com.shashi.minipay.payment.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ProviderRefundRequest(
        @JsonProperty("chargeId")
        String chargeId,

        @JsonProperty("amount")
        BigDecimal amount,

        @JsonProperty("reason")
        String reason
) {
}
