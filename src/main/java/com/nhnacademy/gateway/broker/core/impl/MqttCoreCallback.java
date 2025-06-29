package com.nhnacademy.gateway.broker.core.impl;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttCoreCallback implements MqttCallbackExtended {

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("[core broker] connection complete (reconnect: {})", reconnect);
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.error("[core broker] connection lost: {}", cause.getMessage(), cause);
        /// TODO: 재연결 시도하는 로직 추가
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        log.warn("[core broker] 메세지를 받는 책임을 수행하지 않습니다");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.debug("[core broker] message send");
    }
}
