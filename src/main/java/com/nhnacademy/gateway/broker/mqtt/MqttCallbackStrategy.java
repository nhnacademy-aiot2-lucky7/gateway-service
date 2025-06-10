package com.nhnacademy.gateway.broker.mqtt;

import com.nhnacademy.gateway.broker.BrokerType;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;

public interface MqttCallbackStrategy {

    BrokerType getBrokerType();

    MqttCallbackExtended getMqttCallbackImpl();
}
