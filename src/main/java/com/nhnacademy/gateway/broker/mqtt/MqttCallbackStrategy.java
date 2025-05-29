package com.nhnacademy.gateway.broker.mqtt;

import com.nhnacademy.gateway.broker.mqtt.dto.BrokerType;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;

public interface MqttCallbackStrategy {

    BrokerType getBrokerType();

    MqttCallbackExtended getMqttCallbackImpl();
}
