package com.nhnacademy.gateway.mqtt.receivedata.receiver;

public interface GatewayReceiver {
    void start(String gateBrokerUrl, String gatewayId, String topic);
}
