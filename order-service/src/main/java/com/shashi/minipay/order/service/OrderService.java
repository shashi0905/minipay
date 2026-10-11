package com.shashi.minipay.order.service;

import com.shashi.minipay.order.client.PaymentServiceClient;
import com.shashi.minipay.order.client.dto.CreatePaymentRequest;
import com.shashi.minipay.order.client.dto.CreatePaymentResponse;
import com.shashi.minipay.order.dto.request.CreateOrderRequest;
import com.shashi.minipay.order.dto.request.OrderItemRequest;
import com.shashi.minipay.order.dto.request.ProcessPaymentRequest;
import com.shashi.minipay.order.dto.response.CreateOrderResponse;
import com.shashi.minipay.order.dto.response.OrderItemResponse;
import com.shashi.minipay.order.entity.Order;
import com.shashi.minipay.order.entity.OrderItem;
import com.shashi.minipay.order.entity.OrderStatus;
import com.shashi.minipay.order.exception.InvalidOrderStateException;
import com.shashi.minipay.order.exception.OrderNotFoundException;
import com.shashi.minipay.order.exception.PaymentServiceException;
import com.shashi.minipay.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentServiceClient paymentServiceClient;

    public OrderService(OrderRepository orderRepository, PaymentServiceClient paymentServiceClient) {
        this.orderRepository = orderRepository;
        this.paymentServiceClient = paymentServiceClient;
    }

    /**
     * Create a new order with items.
     * Generates a unique order number and sets initial status to PENDING.
     */
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(request.userId());
        order.setTotalAmount(request.totalAmount());
        order.setCurrency(request.currency());
        order.setStatus(OrderStatus.PENDING);

        Instant now = Instant.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        // Add order items
        for (OrderItemRequest itemRequest : request.items()) {
            OrderItem item = new OrderItem();
            item.setProductId(itemRequest.productId());
            item.setProductName(itemRequest.productName());
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(itemRequest.unitPrice());
            item.setTotalPrice(itemRequest.unitPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
            order.addItem(item);
        }

        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    /**
     * Get order by order ID.
     */
    public CreateOrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        return toResponse(order);
    }

    /**
     * Get order by order number.
     */
    public CreateOrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));
        return toResponse(order);
    }

    /**
     * Get all orders for a specific user.
     */
    public List<CreateOrderResponse> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a payment for an order.
     * Calls the payment service to create a payment record.
     */
    @Transactional
    public CreateOrderResponse createPaymentForOrder(UUID orderId, String idempotencyKey) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStateException(
                    "Cannot create payment for order with status: " + order.getStatus() +
                    ". Orders can only have payments created in PENDING or CONFIRMED state."
            );
        }

        try {
            CreatePaymentRequest paymentRequest = new CreatePaymentRequest(
                    order.getOrderNumber(),
                    order.getUserId(),
                    order.getTotalAmount(),
                    order.getCurrency()
            );

            CreatePaymentResponse paymentResponse = paymentServiceClient
                    .createPayment(paymentRequest, idempotencyKey)
                    .block();

            if (paymentResponse != null) {
                order.setPaymentId(paymentResponse.id().toString());
                order.setStatus(OrderStatus.CONFIRMED);
                order.setUpdatedAt(Instant.now());
                orderRepository.save(order);
            }

            return toResponse(order);
        } catch (PaymentServiceException e) {
            throw new PaymentServiceException("Failed to create payment for order: " + e.getMessage(), e);
        }
    }

    /**
     * Process payment for an order.
     * Calls the payment service to process the payment with the given payment method.
     */
    @Transactional
    public CreateOrderResponse processPaymentForOrder(UUID orderId, ProcessPaymentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (order.getPaymentId() == null) {
            throw new InvalidOrderStateException(
                    "No payment associated with this order. Please create a payment first."
            );
        }

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStateException(
                    "Cannot process payment for order with status: " + order.getStatus() +
                    ". Payments can only be processed for orders in CONFIRMED state."
            );
        }

        try {
            CreatePaymentResponse paymentResponse = paymentServiceClient
                    .processPayment(order.getPaymentId(), request.paymentMethodId())
                    .block();

            if (paymentResponse != null && "SUCCESS".equalsIgnoreCase(paymentResponse.status())) {
                order.setStatus(OrderStatus.PAID);
                order.setUpdatedAt(Instant.now());
                orderRepository.save(order);
            }

            return toResponse(order);
        } catch (PaymentServiceException e) {
            throw new PaymentServiceException("Failed to process payment for order: " + e.getMessage(), e);
        }
    }

    /**
     * Cancel an order.
     * Orders can only be cancelled if they are in PENDING or CONFIRMED state.
     */
    @Transactional
    public CreateOrderResponse cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.REFUNDED) {
            throw new InvalidOrderStateException(
                    "Cannot cancel order with status: " + order.getStatus() +
                    ". Orders can only be cancelled in PENDING or CONFIRMED state."
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        return toResponse(order);
    }

    private String generateOrderNumber() {
        String orderNumber;
        do {
            orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (orderRepository.existsByOrderNumber(orderNumber));
        return orderNumber;
    }

    private CreateOrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
                .collect(Collectors.toList());

        return new CreateOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getStatus(),
                order.getPaymentId(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemResponses
        );
    }
}
