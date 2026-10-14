package com.shashi.minipay.order.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemEvent(
        UUID itemId,
        String productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}
