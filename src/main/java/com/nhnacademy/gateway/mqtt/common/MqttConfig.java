package com.nhnacademy.gateway.mqtt.common;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MqttConfig {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    private static final String CLIENT_ID = "spring-mqtt-client";

    private MqttClient client;

    @Bean(name = "listenerMqttClient")
    public MqttClient listenerMqttClient() throws MqttException {
        MqttClient client = new MqttClient(brokerUrl, "LISTENER_CLIENT", new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        client.connect(options);
        return client;
    }

    @Bean(name = "dummyPublisherMqttClient")
    public MqttClient dummyPublisherMqttClient() throws MqttException {
        MqttClient client = new MqttClient(brokerUrl, "DUMMY_PUB_CLIENT", new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        client.connect(options);
        return client;
    }

    @PreDestroy
    public void cleanup() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
                client.close();
                log.info("MQTT 클라이언트 연결을 정상적으로 종료했습니다.");
            } catch (MqttException e) {
                log.warn("MQTT 클라이언트 종료 중 오류가 발생했습니다: {}", e.getMessage());
            }
        } else {
            log.info("MQTT 클라이언트가 연결되어 있지 않아 종료할 필요가 없습니다.");
        }
    }
}