package com.nhnacademy.gateway.broker.mqtt.impl;

import com.nhnacademy.gateway.broker.mqtt.MqttActionType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttInboundActionListener implements IMqttActionListener {

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        MqttActionType mqttActionType =
                MqttActionType.of(asyncActionToken.getUserContext());
        if (mqttActionType.isConnectionAction()) {
            log.info("[inbound broker] {} success: {}",
                    mqttActionType.action(),
                    asyncActionToken.getClient().getClientId()
            );
        } else {
            log.warn("[inbound broker] 예기치 못한 성공 로그 시도");
        }
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        MqttActionType mqttActionType =
                MqttActionType.of(asyncActionToken.getUserContext());
        if (mqttActionType.isConnectionAction()) {
            /// TODO: 재연결 로직 추가 예정
            log.info("[inbound broker] {} failure: {}",
                    mqttActionType.action(),
                    asyncActionToken.getClient().getClientId()
            );
        } else {
            log.warn("[inbound broker] 예기치 못한 실패 로그 시도");
        }
    }
}
