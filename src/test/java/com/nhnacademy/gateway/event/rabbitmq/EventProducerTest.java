package com.nhnacademy.gateway.event.rabbitmq;

import com.nhnacademy.gateway.event.dto.EventCreateRequest;
import com.nhnacademy.gateway.exception.InvalidMessagingConfigurationException;
import com.nhnacademy.gateway.exception.RabbitMessageSendFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.springframework.amqp.AmqpException;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventProducerTest {

    private RabbitTemplate rabbitTemplate;
    private EventProducer eventProducer;

    private final String exchange = "test.exchange";
    private final String routingKey = "test.routingKey";

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        eventProducer = new EventProducer(rabbitTemplate);

        // private @Value 필드 수동 설정
        setPrivateField(eventProducer, "exchange", exchange);
        setPrivateField(eventProducer, "routingKey", routingKey);
    }

    @Test
    @DisplayName("이벤트 전송 테스트 - convertAndSend 호출 확인")
    void sendEventTest() {
        // given
        EventCreateRequest request = new EventCreateRequest(
                "HIGH",
                "온도 초과",
                "sensor-01",
                "부서-A",
                LocalDateTime.now()
        );

        // when
        eventProducer.sendEvent(request);

        // then
        ArgumentCaptor<EventCreateRequest> captor = ArgumentCaptor.forClass(EventCreateRequest.class);
        verify(rabbitTemplate, times(1))
                .convertAndSend(any(String.class), any(String.class), captor.capture());

        EventCreateRequest sentRequest = captor.getValue();
        assertThat(sentRequest.getEventLevel()).isEqualTo("HIGH");
        assertThat(sentRequest.getEventDetails()).isEqualTo("온도 초과");
        assertThat(sentRequest.getSourceId()).isEqualTo("sensor-01");
        assertThat(sentRequest.getDepartmentId()).isEqualTo("부서-A");
    }

    @Test
    @DisplayName("exchange나 routingKey가 null이면 예외를 던진다")
    void sendEvent_invalidConfig_throwsException() {
        // given
        setPrivateField(eventProducer, "exchange", null); // 일부러 null 설정

        EventCreateRequest request = new EventCreateRequest(
                "MEDIUM", "적정", "sensor-02", "부서-B", LocalDateTime.now()
        );

        // when & then
        assertThrows(InvalidMessagingConfigurationException.class, () -> {
            eventProducer.sendEvent(request);
        });
    }

    @Test
    @DisplayName("AmqpException이 발생하면 RabbitMessageSendFailedException을 던진다")
    void sendEvent_throwAmqpException() {
        // given
        EventCreateRequest request = new EventCreateRequest(
                "LOW", "정상", "sensor-03", "부서-C", LocalDateTime.now()
        );

        doThrow(new AmqpException("테스트")).when(rabbitTemplate)
                .convertAndSend(any(String.class), any(String.class), any(EventCreateRequest.class));

        // when & then
        assertThrows(RabbitMessageSendFailedException.class, () -> {
            eventProducer.sendEvent(request);
        });
    }

    // 리플렉션 유틸
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            var field = EventProducer.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
