package com.nhnacademy.gateway.broker.core.impl;

import com.nhnacademy.gateway.broker.mqtt.MqttCallbackStrategy;
import com.nhnacademy.gateway.broker.mqtt.dto.BrokerType;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MqttCoreCallbackStrategy implements MqttCallbackStrategy {

    private final MqttCallbackExtended mqttCoreCallback;

    public MqttCoreCallbackStrategy(
            @Qualifier("mqttCoreCallback") MqttCallbackExtended mqttCoreCallback
    ) {
        this.mqttCoreCallback = mqttCoreCallback;
    }

    @Override
    public BrokerType getBrokerType() {
        return BrokerType.CORE;
    }

    @Override
    public MqttCallbackExtended getMqttCallbackImpl() {
        return mqttCoreCallback;
    }
}
