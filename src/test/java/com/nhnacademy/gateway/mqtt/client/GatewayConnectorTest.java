package com.nhnacademy.gateway.mqtt.client;

import com.nhnacademy.gateway.mqtt.receivedata.dto.DataRequest;
import com.nhnacademy.gateway.mqtt.receivedata.receiver.impl.CoapDataReceiver;
import com.nhnacademy.gateway.mqtt.receivedata.receiver.impl.HttpDataReceiver;
import com.nhnacademy.gateway.mqtt.receivedata.receiver.impl.MqttDataReceiver;
import org.eclipse.paho.client.mqttv3.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        // 스케줄러가 남아 있으면 종료
        connector.shutdown("gw1");
    }

    @Test
    void startGateway_invokesCorrectReceiver_andSchedulesPublish() {
        // MQTT
        connector.startGateway("tcp://test:1883", "gw1", "MQTT");
        verify(mqttReceiver).start("tcp://test:1883", "gw1", "gw1/data");

        // HTTP
        connector.startGateway("url", "gw2", "HTTP");
        verify(httpReceiver).start("url", "gw2", "gw2/data");

        // COAP
        connector.startGateway("url", "gw3", "COAP");
        verify(coapReceiver).start("url", "gw3", "gw3/data");

        // 잘못된 프로토콜인 경우 아무 호출도 안 함
        connector.startGateway("u", "gwX", "UNKNOWN");
        verifyNoMoreInteractions(mqttReceiver, httpReceiver, coapReceiver);
    }

    @Test
    void receiveGatewayData_enqueueDataRequest() throws Exception {
        DataRequest req = new DataRequest("t1", 1L, 2.2);
        connector.receiveGatewayData("t1", req);

        // 내부 큐에 들어갔는지 반사적으로 publishToHub()로 꺼내보도록
        // 빈 큐면 publishToHub 바로 리턴하므로, 먼저 큐에 넣고 직접 publishToHub 호출
        // 리플렉션 활용해 private 메서드 호출
        var m = GatewayConnector.class.getDeclaredMethod("publishToHub", String.class);
        m.setAccessible(true);

        // MockedConstruction 으로 허브 클라이언트 모의
        try (MockedConstruction<MqttClient> mockClient = mockConstruction(MqttClient.class,
                (mc, ctx) -> {
                    // connect & publish 만 모의
                })) {

            // publishToHub 실행
            m.invoke(connector, "gw1");

            MqttClient hubClient = mockClient.constructed().get(0);
            // publish 호출 검증
            verify(hubClient).publish(eq("project-data/gw1"), any(MqttMessage.class));
        }
    }

    @Test
    void publishToHub_emptyQueue_doesNothing() throws Exception {
        var m = GatewayConnector.class.getDeclaredMethod("publishToHub", String.class);
        m.setAccessible(true);

        // MockedConstruction 으로 허브 클라이언트 생성 감지
        try (MockedConstruction<MqttClient> mockClient = mockConstruction(MqttClient.class)) {
            m.invoke(connector, "gw1");
            // publish 자체가 절대 호출되지 않아야 함
            assertThat(mockClient.constructed()).isEmpty();
        }
    }

    @Test
    void shutdown_cleansUpSchedulerAndClient() throws Exception {
        // 1) 준비: startGateway 로 스케줄러 등록
        connector.startGateway("url", "gw1", "MQTT");
        // 스케줄러가 map에 존재해야 함
        // 2) publishToHub 스케줄이 실행되면 안 되도록 바로 shutdown
        connector.shutdown("gw1");

        // 스케줄러가 종료되었는지 확인 (반사로 들여다보기)
        var schedMapF = GatewayConnector.class.getDeclaredField("schedulerMap");
        schedMapF.setAccessible(true);
        var schedMap = (Map<String, ScheduledExecutorService>) schedMapF.get(connector);
        assertThat(schedMap).doesNotContainKey("gw1");

        // 클라이언트 map에도 없애는 로직 테스트
        var clientMapF = GatewayConnector.class.getDeclaredField("clientMap");
        clientMapF.setAccessible(true);
        var clientMap = (Map<String, MqttClient>) clientMapF.get(connector);
        assertThat(clientMap).doesNotContainKey("gw1");
    }
}
