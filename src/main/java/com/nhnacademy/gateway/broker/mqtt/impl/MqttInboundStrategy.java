package com.nhnacademy.gateway.broker.mqtt.impl;

import com.nhnacademy.gateway.broker.mqtt.MqttCallbackStrategy;
import com.nhnacademy.gateway.broker.mqtt.dto.BrokerType;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MqttInboundStrategy implements MqttCallbackStrategy {

    private final MqttCallbackExtended mqttInboundCallback;

    public MqttInboundStrategy(
            @Qualifier("mqttInboundCallback") MqttCallbackExtended mqttInboundCallback
    ) {
        this.mqttInboundCallback = mqttInboundCallback;
    }

    @Override
    public BrokerType getBrokerType() {
        return BrokerType.INBOUND;
    }

    @Override
    public MqttCallbackExtended getMqttCallbackImpl() {
        return mqttInboundCallback;
    }
}
