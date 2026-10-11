package com.shashi.minipay.order.dto.response;

import com.shashi.minipay.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateOrderResponse(
        UUID id,
        String orderNumber,
        String userId,
        BigDecimal totalAmount,
        String currency,
        OrderStatus status,
        String paymentId,
        Instant createdAt,
        Instant updatedAt,
        List<OrderItemResponse> items
) {
}
