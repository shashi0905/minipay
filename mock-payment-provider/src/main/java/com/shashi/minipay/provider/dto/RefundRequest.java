package com.shashi.minipay.provider.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RefundRequest(
        @NotBlank
        String chargeId,

        @NotNull
        @Positive
        BigDecimal amount,

        @NotBlank
        String reason
) {
}
