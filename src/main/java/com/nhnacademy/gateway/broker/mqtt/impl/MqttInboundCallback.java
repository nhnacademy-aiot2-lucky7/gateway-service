package com.nhnacademy.gateway.broker.mqtt.impl;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttInboundCallback implements MqttCallbackExtended {

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("연결 완료: {} (재연결 여부: {})", serverURI, reconnect);
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("연결이 끊겼습니다: {}", cause.getMessage());
        /// TODO: 재연결 시도하는 로직 추가
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        log.info("[수신] Topic: {}, Payload: {}", topic, message.getPayload());
        // TODO: Team Broker로 전달하거나 내부 처리
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.debug("inbound brokers는 메세지를 전달하는 책임을 수행하지 않습니다");
    }
}
