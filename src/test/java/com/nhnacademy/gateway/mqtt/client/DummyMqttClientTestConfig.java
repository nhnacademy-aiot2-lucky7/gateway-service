package com.nhnacademy.gateway.mqtt.client;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class DummyMqttClientTestConfig {

    @Bean
    public DummyMqttClient dummyMqttClient() {
        return new DummyMqttClient();  // 만약 생성자에 파라미터 있으면 그에 맞게
    }
}