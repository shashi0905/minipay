package com.shashi.minipay.fulfillment.service;

import com.shashi.minipay.fulfillment.entity.FulfillmentStatus;
import com.shashi.minipay.fulfillment.entity.OrderFulfillment;
import com.shashi.minipay.fulfillment.event.OrderPaidEvent;
import com.shashi.minipay.fulfillment.repository.OrderFulfillmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrderFulfillmentService {

    private static final Logger log = LoggerFactory.getLogger(OrderFulfillmentService.class);

    private final OrderFulfillmentRepository fulfillmentRepository;

    public OrderFulfillmentService(OrderFulfillmentRepository fulfillmentRepository) {
        this.fulfillmentRepository = fulfillmentRepository;
    }

    /**
     * Create a new fulfillment record when an order is paid.
     */
    @Transactional
    public OrderFulfillment createFulfillment(OrderPaidEvent event) {
        log.info("Creating fulfillment for order: {}", event.orderNumber());

        // Check if fulfillment already exists
        if (fulfillmentRepository.existsByOrderNumber(event.orderNumber())) {
            log.warn("Fulfillment already exists for order: {}, skipping", event.orderNumber());
            return fulfillmentRepository.findByOrderNumber(event.orderNumber()).orElse(null);
        }

        OrderFulfillment fulfillment = new OrderFulfillment();
        fulfillment.setOrderNumber(event.orderNumber());
        fulfillment.setUserId(event.userId());
        fulfillment.setPaymentId(event.paymentId());
        fulfillment.setStatus(FulfillmentStatus.PENDING);

        Instant now = Instant.now();
        fulfillment.setCreatedAt(now);
        fulfillment.setUpdatedAt(now);

        OrderFulfillment saved = fulfillmentRepository.save(fulfillment);
        log.info("Fulfillment created with ID: {} for order: {}", saved.getId(), event.orderNumber());

        return saved;
    }

    /**
     * Process fulfillment - move from PENDING to PROCESSING.
     */
    @Transactional
    public OrderFulfillment processFulfillment(String orderNumber) {
        OrderFulfillment fulfillment = fulfillmentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Fulfillment not found for order: " + orderNumber));

        if (fulfillment.getStatus() != FulfillmentStatus.PENDING) {
            throw new RuntimeException("Cannot process fulfillment with status: " + fulfillment.getStatus());
        }

        fulfillment.setStatus(FulfillmentStatus.PROCESSING);
        fulfillment.setUpdatedAt(Instant.now());
        fulfillmentRepository.save(fulfillment);

        log.info("Fulfillment moved to PROCESSING for order: {}", orderNumber);
        return fulfillment;
    }

    /**
     * Mark fulfillment as shipped.
     */
    @Transactional
    public OrderFulfillment shipFulfillment(String orderNumber, String trackingNumber) {
        OrderFulfillment fulfillment = fulfillmentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Fulfillment not found for order: " + orderNumber));

        if (fulfillment.getStatus() != FulfillmentStatus.PROCESSING) {
            throw new RuntimeException("Cannot ship fulfillment with status: " + fulfillment.getStatus());
        }

        fulfillment.setStatus(FulfillmentStatus.SHIPPED);
        fulfillment.setTrackingNumber(trackingNumber);
        fulfillment.setShippedAt(Instant.now());
        fulfillment.setUpdatedAt(Instant.now());
        fulfillmentRepository.save(fulfillment);

        log.info("Fulfillment marked as SHIPPED for order: {} with tracking: {}", orderNumber, trackingNumber);
        return fulfillment;
    }

    /**
     * Mark fulfillment as delivered.
     */
    @Transactional
    public OrderFulfillment deliverFulfillment(String orderNumber) {
        OrderFulfillment fulfillment = fulfillmentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Fulfillment not found for order: " + orderNumber));

        if (fulfillment.getStatus() != FulfillmentStatus.SHIPPED) {
            throw new RuntimeException("Cannot mark as delivered with status: " + fulfillment.getStatus());
        }

        fulfillment.setStatus(FulfillmentStatus.DELIVERED);
        fulfillment.setDeliveredAt(Instant.now());
        fulfillment.setUpdatedAt(Instant.now());
        fulfillmentRepository.save(fulfillment);

        log.info("Fulfillment marked as DELIVERED for order: {}", orderNumber);
        return fulfillment;
    }

    /**
     * Mark fulfillment as failed.
     */
    @Transactional
    public OrderFulfillment failFulfillment(String orderNumber) {
        OrderFulfillment fulfillment = fulfillmentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Fulfillment not found for order: " + orderNumber));

        fulfillment.setStatus(FulfillmentStatus.FAILED);
        fulfillment.setUpdatedAt(Instant.now());
        fulfillmentRepository.save(fulfillment);

        log.error("Fulfillment marked as FAILED for order: {}", orderNumber);
        return fulfillment;
    }

    /**
     * Get fulfillment by order number.
     */
    public OrderFulfillment getFulfillmentByOrderNumber(String orderNumber) {
        return fulfillmentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Fulfillment not found for order: " + orderNumber));
    }
}
