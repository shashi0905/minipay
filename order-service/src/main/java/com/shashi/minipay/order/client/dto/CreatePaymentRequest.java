package com.shashi.minipay.order.client.dto;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        String orderId,
        String userId,
        BigDecimal amount,
        String currency
) {
}
