package com.shashi.minipay.order.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.order-paid}")
    private String orderPaidExchange;

    @Value("${rabbitmq.routing-key.order-paid}")
    private String orderPaidRoutingKey;

    @Bean
    public DirectExchange orderPaidExchange() {
        return new DirectExchange(orderPaidExchange, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
