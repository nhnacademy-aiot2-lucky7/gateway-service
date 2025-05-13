package com.nhnacademy.gateway.event.rabbitMq;

import com.nhnacademy.gateway.event.dto.EventCreateRequest;
import com.nhnacademy.gateway.event.rabbitmq.EventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EventProducerTest {

    private RabbitTemplate rabbitTemplate;
    private EventProducer eventProducer;

    private final String exchange = "test.exchange";
    private final String routingKey = "test.routingKey";

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        eventProducer = new EventProducer(rabbitTemplate);

        // private 필드 설정 (reflection 사용)
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
                .convertAndSend(eq(exchange), eq(routingKey), captor.capture());

        EventCreateRequest sentRequest = captor.getValue();
        assertThat(sentRequest.getEventLevel()).isEqualTo("HIGH");
        assertThat(sentRequest.getEventDetails()).isEqualTo("온도 초과");
        assertThat(sentRequest.getSourceId()).isEqualTo("sensor-01");
        assertThat(sentRequest.getDepartmentId()).isEqualTo("부서-A");
    }

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