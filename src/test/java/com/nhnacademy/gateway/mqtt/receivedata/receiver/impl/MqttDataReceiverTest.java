package com.nhnacademy.gateway.mqtt.receivedata.receiver.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.gateway.mqtt.receivedata.dto.DataRequest;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
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

    private final String brokerUrl = "tcp://localhost:1883";
    private final String gatewayId = "gw01";
    private final String topic = "gw01/data";

    @Test
    void start_subscribeAndReceiveValidMessage_shouldParseCorrectly() throws Exception {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        MqttDataReceiver receiver = new MqttDataReceiver(objectMapper);

        AtomicReference<DataRequest> capturedData = new AtomicReference<>();

        try (MockedConstruction<MqttClient> mocked = mockConstruction(MqttClient.class,
                (mockClient, context) -> {
                    doAnswer(invocation -> {
                        String subscribedTopic = invocation.getArgument(0);
                        IMqttMessageListener listener = invocation.getArgument(1);

                        DataRequest req = new DataRequest(topic, 1111L, 42.0);
                        String payload = objectMapper.writeValueAsString(req);
                        MqttMessage message = new MqttMessage(payload.getBytes());

                        // simulate message reception
                        listener.messageArrived(subscribedTopic, message);

                        // deserialize again for verification
                        capturedData.set(objectMapper.readValue(payload, DataRequest.class));

                        return null;
                    }).when(mockClient).subscribe(eq(topic), any(IMqttMessageListener.class));
                })) {

            // when
            receiver.start(brokerUrl, gatewayId, topic);

            // then
            assertThat(capturedData.get()).isNotNull();
            DataRequest result = capturedData.get();
            assertThat(result.getTopic()).isEqualTo(topic);
            assertThat(result.getTime()).isEqualTo(1111L);
            assertThat(result.getValue()).isEqualTo(42.0);
        }
    }

    @Test
    void start_subscribeWithInvalidJson_shouldNotThrowButLog() throws Exception {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        MqttDataReceiver receiver = new MqttDataReceiver(objectMapper);

        try (MockedConstruction<MqttClient> mocked = mockConstruction(MqttClient.class,
                (mockClient, context) -> {
                    doAnswer(invocation -> {
                        String subscribedTopic = invocation.getArgument(0);
                        IMqttMessageListener listener = invocation.getArgument(1);

                        // simulate invalid JSON
                        MqttMessage badMsg = new MqttMessage("not-a-json".getBytes());
                        listener.messageArrived(subscribedTopic, badMsg);

                        return null;
                    }).when(mockClient).subscribe(eq(topic), any(IMqttMessageListener.class));
                })) {

            // when / then: 예외는 발생하지 않음 (내부에서 try-catch 처리되므로)
            receiver.start(brokerUrl, gatewayId, topic);
        }
    }

    @Test
    void start_connectionFailure_shouldThrowRuntimeException() throws Exception {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        MqttDataReceiver receiver = new MqttDataReceiver(objectMapper);

        try (MockedConstruction<MqttClient> mocked = mockConstruction(MqttClient.class,
                (mockClient, context) -> {
                    doThrow(new MqttException(123)).when(mockClient).connect(any(MqttConnectOptions.class));
                })) {

            // when & then
            assertThrows(RuntimeException.class, () -> receiver.start(brokerUrl, gatewayId, topic));
        }
    }
}
