package com.nhnacademy.gateway.mqtt.receivedata.receiver.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.gateway.mqtt.receivedata.dto.DataRequest;
import org.eclipse.paho.client.mqttv3.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MqttDataReceiverTest {

    private final String brokerUrl = "tcp://172.19.0.3:1883";
    private final String gatewayId = "gw01";
    private final String topic     = "gw01/data";

    @Test
    void start_subscribeAndReceiveValidMessage_shouldParseCorrectly() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        MqttDataReceiver receiver = new MqttDataReceiver(objectMapper);

        AtomicReference<DataRequest> capturedData = new AtomicReference<>();

        try (MockedConstruction<MqttClient> mocked = mockConstruction(
                MqttClient.class,
                (mockClient, context) -> {
                    // 1) connect() 호출은 무시
                    doNothing().when(mockClient).connect(any(MqttConnectOptions.class));

                    // 2) subscribe() 시 listener 호출만 시뮬레이션
                    doAnswer(invocation -> {
                        String subscribedTopic = invocation.getArgument(0);
                        IMqttMessageListener listener = invocation.getArgument(1);

                        DataRequest req = new DataRequest(topic, 1111L, 42.0);
                        String payload = objectMapper.writeValueAsString(req);
                        MqttMessage message = new MqttMessage(payload.getBytes());

                        listener.messageArrived(subscribedTopic, message);
                        capturedData.set(objectMapper.readValue(payload, DataRequest.class));
                        return null;
                    }).when(mockClient).subscribe(eq(topic), any(IMqttMessageListener.class));
                }
        )) {
            // when
            receiver.start(brokerUrl, gatewayId, topic);

            // then
            DataRequest result = capturedData.get();
            assertThat(result).isNotNull();
            assertThat(result.getTopic()).isEqualTo(topic);
            assertThat(result.getTime()).isEqualTo(1111L);
            assertThat(result.getValue()).isEqualTo(42.0);
        }
    }

    @Test
    void start_subscribeWithInvalidJson_shouldNotThrowButLog() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        MqttDataReceiver receiver = new MqttDataReceiver(objectMapper);

        try (MockedConstruction<MqttClient> mocked = mockConstruction(
                MqttClient.class,
                (mockClient, context) -> {
                    // connect()도 stub 처리
                    doNothing().when(mockClient).connect(any(MqttConnectOptions.class));

                    // invalid JSON 메시지 시뮬레이션
                    doAnswer(invocation -> {
                        String subscribedTopic = invocation.getArgument(0);
                        IMqttMessageListener listener = invocation.getArgument(1);

                        MqttMessage badMsg = new MqttMessage("not-a-json".getBytes());
                        listener.messageArrived(subscribedTopic, badMsg);
                        return null;
                    }).when(mockClient).subscribe(eq(topic), any(IMqttMessageListener.class));
                }
        )) {
            // when / then: 예외 없이 수행되어야 합니다.
            receiver.start(brokerUrl, gatewayId, topic);
        }
    }

    @Test
    void start_connectionFailure_shouldThrowRuntimeException() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        MqttDataReceiver receiver = new MqttDataReceiver(objectMapper);

        try (MockedConstruction<MqttClient> mocked = mockConstruction(
                MqttClient.class,
                (mockClient, context) -> {
                    // connect()에서 예외 던짐
                    doThrow(new MqttException(123))
                            .when(mockClient).connect(any(MqttConnectOptions.class));
                }
        )) {
            // when & then
            assertThrows(RuntimeException.class,
                    () -> receiver.start(brokerUrl, gatewayId, topic));
        }
    }
}
