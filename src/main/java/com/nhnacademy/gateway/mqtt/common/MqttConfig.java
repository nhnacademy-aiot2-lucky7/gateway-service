package com.nhnacademy.gateway.mqtt.common;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {

    private static final String BROKER = "tcp://172.19.0.3:1883"; // 로컬 환경 테스트 용도

//    private static final String BROKER = "tcp://localhost:10240";
    private static final String CLIENT_ID = "spring-mqtt-client";

    @Bean
    public MqttClient mqttClient() throws MqttException {
        MqttClient client = new MqttClient(BROKER, CLIENT_ID);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        client.connect(options);
        return client;
    }
}