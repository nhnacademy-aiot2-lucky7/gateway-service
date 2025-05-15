package com.nhnacademy.gateway.event.rabbitmq;

import com.nhnacademy.gateway.exception.RabbitConfigurationException;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${mq.exchange.event}")
    private String eventExchange;

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        if (connectionFactory == null) {
            throw new RabbitConfigurationException("RabbitMQ ConnectionFactory 설정이 누락되었습니다.");
        }

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public DirectExchange eventExchange() {
        if (eventExchange == null || eventExchange.isBlank()) {
            throw new RabbitConfigurationException("event.exchange 설정이 비어 있습니다.");
        }
        return new DirectExchange(eventExchange);
    }
}
