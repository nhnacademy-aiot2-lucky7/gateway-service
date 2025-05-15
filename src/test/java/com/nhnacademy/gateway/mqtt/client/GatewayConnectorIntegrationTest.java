package com.nhnacademy.gateway.mqtt.client;

import com.nhnacademy.gateway.mqtt.receivedata.dto.DataRequest;
import com.nhnacademy.gateway.mqtt.receivedata.receiver.impl.CoapDataReceiver;
import com.nhnacademy.gateway.mqtt.receivedata.receiver.impl.HttpDataReceiver;
import com.nhnacademy.gateway.mqtt.receivedata.receiver.impl.MqttDataReceiver;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class GatewayConnectorIntegrationTest {

    @Autowired
    private GatewayConnector connector;

    @MockitoBean
    private MqttDataReceiver mqttReceiver;

    @MockitoBean
    private HttpDataReceiver httpReceiver;

    @MockitoBean
    private CoapDataReceiver coapReceiver;

    @AfterEach
    void tearDown() {
        connector.shutdown("gw1");
        connector.shutdown("gw2");
        connector.shutdown("gw3");
    }

    @Test
    void testStartGateway_mqtt_shouldCallMqttReceiver() {
        connector.startGateway("tcp://broker", "gw1", "MQTT");

        verify(mqttReceiver).start("tcp://broker", "gw1", "gw1/data");
    }

    @Test
    void testStartGateway_http_shouldCallHttpReceiver() {
        connector.startGateway("http://gateway", "gw2", "HTTP");

        verify(httpReceiver).start("http://gateway", "gw2", "gw2/data");
    }

    @Test
    void testStartGateway_coap_shouldCallCoapReceiver() {
        connector.startGateway("coap://gateway", "gw3", "COAP");

        verify(coapReceiver).start("coap://gateway", "gw3", "gw3/data");
    }

    @Test
    void testStartGateway_withInvalidProtocol_shouldNotCallReceivers() {
        connector.startGateway("any://invalid", "gwX", "INVALID");

        verifyNoInteractions(mqttReceiver, httpReceiver, coapReceiver);
    }

    @Test
    void testReceiveGatewayData_andFlushToHub_shouldNotThrow() throws Exception {
        // 1. Data 수신
        DataRequest data = new DataRequest("gw1/data", System.currentTimeMillis(), 25.5);
        connector.receiveGatewayData("gw1/data", data);

        // 2. publishToHub() 호출
        var method = GatewayConnector.class.getDeclaredMethod("publishToHub", String.class);
        method.setAccessible(true);

        try (MockedConstruction<MqttClient> mocked = mockConstruction(MqttClient.class,
                (mock, context) -> {
                    doNothing().when(mock).connect(any());
                    doNothing().when(mock).publish(anyString(), any(MqttMessage.class));
                    doNothing().when(mock).disconnect();
                    doNothing().when(mock).close();
                })) {

            method.invoke(connector, "gw1");

            MqttClient client = mocked.constructed().get(0);
            verify(client).publish(eq("project-data/gw1"), any(MqttMessage.class));
        }
    }

    @Test
    void testShutdown_shouldCleanSchedulerAndClientMap() throws Exception {
        connector.startGateway("tcp://broker", "gw1", "MQTT");
        connector.shutdown("gw1");

        var schedulerMapField = GatewayConnector.class.getDeclaredField("schedulerMap");
        schedulerMapField.setAccessible(true);
        var schedulerMap = (Map<String, ScheduledExecutorService>) schedulerMapField.get(connector);
        assertThat(schedulerMap).doesNotContainKey("gw1");

        var clientMapField = GatewayConnector.class.getDeclaredField("clientMap");
        clientMapField.setAccessible(true);
        var clientMap = (Map<String, MqttClient>) clientMapField.get(connector);
        assertThat(clientMap).doesNotContainKey("gw1");
    }

    @Test
    void testFlushToHub_emptyQueue_shouldNotThrow() throws Exception {
        var method = GatewayConnector.class.getDeclaredMethod("publishToHub", String.class);
        method.setAccessible(true);

        try (MockedConstruction<MqttClient> mocked = mockConstruction(MqttClient.class)) {
            method.invoke(connector, "gw1");
            assertThat(mocked.constructed()).isEmpty(); // 아무 클라이언트도 생성되지 않아야 함
        }
    }
}
