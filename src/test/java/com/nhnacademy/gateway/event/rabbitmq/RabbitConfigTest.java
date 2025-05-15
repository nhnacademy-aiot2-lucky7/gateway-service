package com.nhnacademy.gateway.event.rabbitmq;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class RabbitConfigTest {

    @Test
    void rabbitTemplate_ShouldUseJackson2JsonMessageConverter() {

        ConnectionFactory factory = mock(ConnectionFactory.class);
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        RabbitConfig config = new RabbitConfig();

        RabbitTemplate template = config.rabbitTemplate(factory, converter);

        assertSame(converter, template.getMessageConverter());
    }

    @Test
    void eventExchange_ShouldBeCreatedWithCorrectName() throws Exception {

        RabbitConfig config = new RabbitConfig();
        Field field = RabbitConfig.class.getDeclaredField("eventExchange");
        field.setAccessible(true);
        field.set(config, "my.exchange");

        DirectExchange exchange = config.eventExchange();

        assertEquals("my.exchange", exchange.getName());
    }

}