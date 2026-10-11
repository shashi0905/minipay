package com.shashi.minipay.order.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProcessPaymentRequest(
        @NotBlank
        String paymentMethodId
) {
}
