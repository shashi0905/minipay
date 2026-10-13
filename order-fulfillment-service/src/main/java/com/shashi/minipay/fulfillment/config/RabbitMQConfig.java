package com.shashi.minipay.fulfillment.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.order-paid}")
    private String orderPaidQueue;

    @Value("${rabbitmq.exchange.order-paid}")
    private String orderPaidExchange;

    @Value("${rabbitmq.routing-key.order-paid}")
    private String orderPaidRoutingKey;

    @Bean
    public Queue orderPaidQueue() {
        return new Queue(orderPaidQueue, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
