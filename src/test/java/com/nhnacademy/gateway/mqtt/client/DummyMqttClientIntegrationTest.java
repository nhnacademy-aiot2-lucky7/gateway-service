package com.nhnacademy.gateway.mqtt.client;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(
        classes = {
                com.nhnacademy.gateway.mqtt.common.MqttConfig.class,
                com.nhnacademy.gateway.mqtt.client.DummyMqttClient.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DummyMqttClientIntegrationTest {

    @Autowired
    DummyMqttClient dummyMqttClient;

    private MqttClient testSubscriber;

    @BeforeAll
    void setupSubscriber() throws MqttException {
        testSubscriber = new MqttClient("tcp://172.19.0.2:1883", MqttClient.generateClientId());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        testSubscriber.connect(options);
    }

    @Test
    void testDummyDataPublishing() throws InterruptedException, MqttException {
        String topicPrefix = "dummy_data/s/nhnacademy/b/gyeongnam_campus/p/";
        List<String> receivedMessages = new ArrayList<>();

        testSubscriber.subscribe(topicPrefix + "#", (topic, message) -> {
            receivedMessages.add(new String(message.getPayload()));
        });

        // 테스트 시작 이후 수동으로 발행 시작
        dummyMqttClient.startPublishing();

        Thread.sleep(5000); // 메시지를 받을 시간 대기

        Assertions.assertFalse(receivedMessages.isEmpty(), "MQTT 메시지를 수신하지 못했습니다.");
    }


    @AfterAll
    void tearDown() throws MqttException {
        testSubscriber.disconnect();
        testSubscriber.close();
    }
}
