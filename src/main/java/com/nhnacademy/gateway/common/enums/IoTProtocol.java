package com.nhnacademy.gateway.common.enums;

public enum IoTProtocol {

    MQTT,
    MQTT_TLS;
    /*MODBUS_TCP,
    MODBUS_RTU,
    COAP,
    HTTP,
    HTTPS,
    WEBSOCKET;

    public boolean isSecure() {
        return this == MQTT_TLS || this == HTTPS;
    }*/

    public boolean isSecure() {
        return this == MQTT_TLS;
    }
}
