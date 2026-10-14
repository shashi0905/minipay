package com.shashi.minipay.order.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shashi.minipay.order.event.OrderPaidEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String orderPaidRoutingKey;

    public OrderEventPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${rabbitmq.routing-key.order-paid}") String orderPaidRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.orderPaidRoutingKey = orderPaidRoutingKey;
    }

    public void publishOrderPaidEvent(OrderPaidEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(orderPaidRoutingKey, message);
            log.info("Published OrderPaidEvent for order: {}", event.orderNumber());
        } catch (Exception e) {
            log.error("Failed to publish OrderPaidEvent for order: {}", event.orderNumber(), e);
            throw new RuntimeException("Failed to publish OrderPaidEvent", e);
        }
    }
}
