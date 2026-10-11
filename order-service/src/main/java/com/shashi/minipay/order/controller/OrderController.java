package com.shashi.minipay.order.controller;

import com.shashi.minipay.order.dto.request.CreateOrderRequest;
import com.shashi.minipay.order.dto.request.ProcessPaymentRequest;
import com.shashi.minipay.order.dto.response.CreateOrderResponse;
import com.shashi.minipay.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Create a new order.
     */
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResponse response = orderService.createOrder(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get order by order ID.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<CreateOrderResponse> getOrderById(@PathVariable UUID orderId) {
        CreateOrderResponse response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get order by order number.
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<CreateOrderResponse> getOrderByNumber(@PathVariable String orderNumber) {
        CreateOrderResponse response = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all orders for a specific user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CreateOrderResponse>> getOrdersByUserId(@PathVariable String userId) {
        List<CreateOrderResponse> responses = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Create a payment for an order.
     * The Idempotency-Key header ensures duplicate requests return the same payment.
     */
    @PostMapping("/{orderId}/payment")
    public ResponseEntity<CreateOrderResponse> createPaymentForOrder(
            @PathVariable UUID orderId,
            @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey) {
        CreateOrderResponse response = orderService.createPaymentForOrder(orderId, idempotencyKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Process payment for an order.
     * This endpoint triggers the actual payment processing through the payment service.
     */
    @PostMapping("/{orderId}/payment/process")
    public ResponseEntity<CreateOrderResponse> processPaymentForOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody ProcessPaymentRequest request) {
        CreateOrderResponse response = orderService.processPaymentForOrder(orderId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel an order.
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<CreateOrderResponse> cancelOrder(@PathVariable UUID orderId) {
        CreateOrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }
}
