package com.nhnacademy.gateway.event.rabbitmq;

import com.nhnacademy.gateway.event.dto.EventCreateRequest;
import com.nhnacademy.gateway.exception.InvalidMessagingConfigurationException;
import com.nhnacademy.gateway.exception.RabbitMessageSendFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
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
        if (exchange == null || routingKey == null) {
            throw new InvalidMessagingConfigurationException("Exchange나 RoutingKey가 설정되지 않았습니다.");
        }

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, request);
        } catch (AmqpException e) {
            throw new RabbitMessageSendFailedException();
        }
    }
}
