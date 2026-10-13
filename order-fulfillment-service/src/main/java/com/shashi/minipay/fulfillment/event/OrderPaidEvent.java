package com.shashi.minipay.fulfillment.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderPaidEvent(
        UUID orderId,
        String orderNumber,
        String userId,
        BigDecimal totalAmount,
        String currency,
        String paymentId,
        Instant paymentCompletedAt,
        List<OrderItemEvent> items
) {
}
