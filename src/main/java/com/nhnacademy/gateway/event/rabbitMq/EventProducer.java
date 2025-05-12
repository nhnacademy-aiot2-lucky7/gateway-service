package com.nhnacademy.gateway.event.rabbitMq;

import com.nhnacademy.gateway.event.dto.EventCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${mq.exchange.event}")
    private String exchange;

    @Value("${mq.routing-key.event}")
    private String routingKey;

    public void sendEvent(EventCreateRequest request) {
        rabbitTemplate.convertAndSend(exchange, routingKey, request);
    }
}
