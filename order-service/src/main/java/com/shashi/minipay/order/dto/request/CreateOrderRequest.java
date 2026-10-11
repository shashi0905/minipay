package com.shashi.minipay.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank
        String userId,

        @NotNull
        @Positive
        BigDecimal totalAmount,

        @NotBlank
        @Size(min = 3, max = 3)
        String currency,

        @NotEmpty
        @Valid
        List<OrderItemRequest> items
) {
}
