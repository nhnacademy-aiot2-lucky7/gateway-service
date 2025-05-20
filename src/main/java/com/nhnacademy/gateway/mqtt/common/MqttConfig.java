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

    @Bean
    public MqttClient mqttClient() throws MqttException, InterruptedException {
        client = new MqttClient(brokerUrl, CLIENT_ID, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        options.setMaxInflight(100);

        int attempt = 0;
        while (attempt < 5) {
            try {
                client.connect(options);
                log.info("MQTT 브로커 연결 성공: {}", brokerUrl);
                return client;
            } catch (MqttException e) {
                attempt++;
                log.warn("MQTT 연결 시도 #{} 실패, 2초 후 재시도", attempt);
                Thread.sleep(2000);
            }
        }
        throw new MqttException(new Throwable("MQTT 브로커 연결 재시도 모두 실패"));
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