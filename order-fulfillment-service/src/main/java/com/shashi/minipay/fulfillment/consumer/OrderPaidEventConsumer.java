package com.shashi.minipay.fulfillment.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shashi.minipay.fulfillment.event.OrderPaidEvent;
import com.shashi.minipay.fulfillment.service.OrderFulfillmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPaidEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderPaidEventConsumer.class);

    private final OrderFulfillmentService fulfillmentService;
    private final ObjectMapper objectMapper;

    public OrderPaidEventConsumer(OrderFulfillmentService fulfillmentService, ObjectMapper objectMapper) {
        this.fulfillmentService = fulfillmentService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${rabbitmq.queue.order-paid}")
    public void handleOrderPaidEvent(String message) {
        try {
            log.info("Received OrderPaidEvent: {}", message);
            OrderPaidEvent event = objectMapper.readValue(message, OrderPaidEvent.class);
            fulfillmentService.createFulfillment(event);
            log.info("Successfully processed OrderPaidEvent for order: {}", event.orderNumber());
        } catch (Exception e) {
            log.error("Error processing OrderPaidEvent: {}", message, e);
            // In production, you might want to send to a dead-letter queue
            throw new RuntimeException("Failed to process OrderPaidEvent", e);
        }
    }
}
