package com.nhnacademy.gateway.mqtt.listener;

import com.nhnacademy.gateway.gate.dto.GateRequest;
import com.nhnacademy.gateway.gate.service.GateService;
import com.nhnacademy.gateway.user.common.UserContextHolder;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MqttListenerTest {
    @Mock
    MqttClient mqttClient;

    @Mock
    GateService gateService;

    @InjectMocks
    MqttListener listener;

    // 싱글스레드 직접 실행 해주는 executor
    ExecutorService directExecutor = Executors.newSingleThreadExecutor();

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // registerGateway() 에서 gateService.createGate(...) stub
        when(gateService.createGate(any(GateRequest.class))).thenReturn(123L);

        // 리플렉션으로 private final ExecutorService executor 교체
        Field execField = MqttListener.class.getDeclaredField("executor");
        execField.setAccessible(true);
        execField.set(listener, directExecutor);

        // 유저 컨텍스트 초기화
        UserContextHolder.setDepartmentId("master");

        // 구독 등록: 이 안에서 subscribe(...)가 호출된다
        listener.subscribe();
    }

    @AfterEach
    void tearDown() {
        directExecutor.shutdownNow();
    }

    @Test
    void subscribe_shouldRegisterAndSubscribe() throws Exception {
        // gateService.createGate 가 한 번 실행되어 gateId 설정됨
        verify(gateService, times(1)).createGate(any(GateRequest.class));
        // mqttClient.subscribe 호출 검증
        verify(mqttClient).subscribe(eq("dummy_data/#"), any(IMqttMessageListener.class));
    }

    @Test
    void onValidMessage_shouldPublishRewrittenTopic() throws Exception {
        // 1) subscribe 때 등록된 IMqttMessageListener 캡처
        ArgumentCaptor<IMqttMessageListener> captor =
                ArgumentCaptor.forClass(IMqttMessageListener.class);
        verify(mqttClient).subscribe(eq("dummy_data/#"), captor.capture());
        IMqttMessageListener messageListener = captor.getValue();

        // 2) 유효한 토픽과 페이로드 준비
        String originalTopic =
                "dummy_data/s/nhnacademy/b/gyeongnam_campus/p/office/env/d/device123/n/위치2/e/humidity";
        String payload = "{\"time\":5555,\"value\":77.7}";
        MqttMessage msg = new MqttMessage(payload.getBytes());

        // 3) 콜백 호출
        messageListener.messageArrived(originalTopic, msg);

        // 4) 비동기로 실행된 publish를 500ms까지 대기하며 검증
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MqttMessage> msgCaptor = ArgumentCaptor.forClass(MqttMessage.class);
        verify(mqttClient, timeout(500)).publish(topicCaptor.capture(), msgCaptor.capture());

        String newTopic = topicCaptor.getValue();
        String newPayload = new String(msgCaptor.getValue().getPayload());

        assertThat(newTopic)
                .isEqualTo("project-data/s/nhnacademy/b/gyeongnam_campus/p/office/env/d/device123/g/123/n/위치2/e/humidity");
        assertThat(newPayload).isEqualTo("{\"time\": 5555, \"value\": 77.70}");
    }


    @Test
    void onBadTopic_shouldNotPublish() throws Exception {
        // 콜백 캡처
        ArgumentCaptor<IMqttMessageListener> captor = ArgumentCaptor.forClass(IMqttMessageListener.class);
        verify(mqttClient).subscribe(eq("dummy_data/#"), captor.capture());
        IMqttMessageListener messageListener = captor.getValue();

        // 잘못된 토픽 구조
        String badTopic = "dummy_data/invalid/format";
        MqttMessage msg = new MqttMessage("{\"time\":1,\"value\":2}".getBytes());

        messageListener.messageArrived(badTopic, msg);

        // publish 절대 호출 안 됨
        verify(mqttClient, never()).publish(anyString(), any(MqttMessage.class));
    }
}
