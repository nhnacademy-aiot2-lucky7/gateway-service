package com.nhnacademy.gateway.mqtt.client;

import com.nhnacademy.gateway.exception.MqttConnectionException;
import com.nhnacademy.gateway.mqtt.receivedata.dto.DataRequest;
import com.nhnacademy.gateway.mqtt.receivedata.receiver.impl.CoapDataReceiver;
import com.nhnacademy.gateway.mqtt.receivedata.receiver.impl.HttpDataReceiver;
import com.nhnacademy.gateway.mqtt.receivedata.receiver.impl.MqttDataReceiver;
import org.eclipse.paho.client.mqttv3.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewayConnectorTest {

    MqttDataReceiver mqttReceiver;
    HttpDataReceiver httpReceiver;
    CoapDataReceiver coapReceiver;
    GatewayConnector connector;

    @BeforeEach
    void setUp() {
        mqttReceiver = mock(MqttDataReceiver.class);
        httpReceiver = mock(HttpDataReceiver.class);
        coapReceiver = mock(CoapDataReceiver.class);
        connector = new GatewayConnector(mqttReceiver, httpReceiver, coapReceiver);
    }

    @AfterEach
    void tearDown() {
        connector.shutdown("gw1");
    }

    @Test
    void startGateway_invokesCorrectReceiver_andSchedulesPublish() {
        connector.startGateway("tcp://test:1883", "gw1", "MQTT");
        verify(mqttReceiver).start("tcp://test:1883", "gw1", "gw1/data");

        connector.startGateway("url", "gw2", "HTTP");
        verify(httpReceiver).start("url", "gw2", "gw2/data");

        connector.startGateway("url", "gw3", "COAP");
        verify(coapReceiver).start("url", "gw3", "gw3/data");

        connector.startGateway("url", "gwX", "UNKNOWN");
        verifyNoMoreInteractions(mqttReceiver, httpReceiver, coapReceiver);
    }

    @Test
    void receiveGatewayData_enqueueDataRequest() throws Exception {
        DataRequest req = new DataRequest("t1", 1L, 2.2);
        connector.receiveGatewayData("t1", req);

        var m = GatewayConnector.class.getDeclaredMethod("publishToHub", String.class);
        m.setAccessible(true);

        try (MockedConstruction<MqttClient> mockClient = mockConstruction(MqttClient.class,
                (mc, ctx) -> {
                    try {
                        doNothing().when(mc).connect(any());
                        doNothing().when(mc).publish(anyString(), any(MqttMessage.class));
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                })) {
            m.invoke(connector, "gw1");

            MqttClient hubClient = mockClient.constructed().get(0);
            verify(hubClient).publish(eq("project-data/gw1"), any(MqttMessage.class));
        }
    }

    @Test
    void publishToHub_emptyQueue_doesNothing() throws Exception {
        var m = GatewayConnector.class.getDeclaredMethod("publishToHub", String.class);
        m.setAccessible(true);

        try (MockedConstruction<MqttClient> mockClient = mockConstruction(MqttClient.class)) {
            m.invoke(connector, "gw1");
            assertThat(mockClient.constructed()).isEmpty();
        }
    }

    @Test
    void publishToHub_mqttConnectionFails_shouldThrowCustomException() throws Exception {
        var m = GatewayConnector.class.getDeclaredMethod("publishToHub", String.class);
        m.setAccessible(true);

        // 강제로 queue에 데이터 하나 추가
        connector.receiveGatewayData("t1", new DataRequest("t1", 1L, 2.2));

        try (MockedConstruction<MqttClient> ignored = mockConstruction(MqttClient.class,
                (mc, ctx) -> {
                    try {
                        doThrow(new MqttException(1)).when(mc).connect(any());
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                })) {
            assertThatThrownBy(() -> m.invoke(connector, "gw1"))
                    .hasCauseInstanceOf(MqttConnectionException.class);
        }
    }

    @Test
    void publishToHub_publishFails_shouldLogError() throws Exception {
        var m = GatewayConnector.class.getDeclaredMethod("publishToHub", String.class);
        m.setAccessible(true);

        connector.receiveGatewayData("t1", new DataRequest("t1", 1L, 2.2));

        try (MockedConstruction<MqttClient> mockClient = mockConstruction(MqttClient.class,
                (mc, ctx) -> {
                    try {
                        doNothing().when(mc).connect(any());
                        doThrow(new MqttException(2)).when(mc).publish(anyString(), any(MqttMessage.class));
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                })) {
            m.invoke(connector, "gw1");

            MqttClient hubClient = mockClient.constructed().get(0);
            verify(hubClient).publish(anyString(), any());
        }
    }

    @Test
    void shutdown_disconnectFails_shouldLogWarning() throws Exception {
        MqttClient mockClient = mock(MqttClient.class);
        when(mockClient.isConnected()).thenReturn(true);

        // lenient로 불필요한 스텁 경고 방지
        lenient().doThrow(new MqttException(0)).when(mockClient).disconnect();
        lenient().doThrow(new MqttException(0)).when(mockClient).close();

        var clientMapField = GatewayConnector.class.getDeclaredField("clientMap");
        clientMapField.setAccessible(true);
//        @SuppressWarnings("unchecked")
        Map<String, MqttClient> clientMap = (Map<String, MqttClient>) clientMapField.get(connector);
        clientMap.put("gw1", mockClient);

        var schedulerMapField = GatewayConnector.class.getDeclaredField("schedulerMap");
        schedulerMapField.setAccessible(true);
//        @SuppressWarnings("unchecked")
        Map<String, ScheduledExecutorService> schedulerMap = (Map<String, ScheduledExecutorService>) schedulerMapField.get(connector);
        schedulerMap.put("gw1", mock(ScheduledExecutorService.class));

        connector.shutdown("gw1");

        assertThat(clientMap).doesNotContainKey("gw1");
        assertThat(schedulerMap).doesNotContainKey("gw1");
    }

    @Test
    void shutdown_cleansUpSchedulerAndClient() throws Exception {
        connector.startGateway("url", "gw1", "MQTT");
        connector.shutdown("gw1");

        var schedMapF = GatewayConnector.class.getDeclaredField("schedulerMap");
        schedMapF.setAccessible(true);
        var schedMap = (Map<String, ScheduledExecutorService>) schedMapF.get(connector);
        assertThat(schedMap).doesNotContainKey("gw1");

        var clientMapF = GatewayConnector.class.getDeclaredField("clientMap");
        clientMapF.setAccessible(true);
        var clientMap = (Map<String, MqttClient>) clientMapF.get(connector);
        assertThat(clientMap).doesNotContainKey("gw1");
    }

    @Test
    void testStartGatewayWithHttp() {
        HttpDataReceiver mockHttpReceiver = mock(HttpDataReceiver.class);
        connector = new GatewayConnector(null, mockHttpReceiver, null);
        connector.startGateway("http://example.com", "gateway123", "HTTP");

        verify(mockHttpReceiver, times(1)).start(anyString(), anyString(), anyString());
    }
}
