package com.nhnacademy.gateway.common.enums;

import java.util.Arrays;

public enum IoTProtocol {

    MQTT,
    MQTT_TLS,
    MODBUS_TCP,
    MODBUS_RTU;
    /*COAP,
    HTTP,
    HTTPS,
    WEBSOCKET;

    public boolean isSecure() {
        return this == MQTT_TLS || this == HTTPS;
    }*/

    public static String[] VALID_VALUES_STRING_ARRAY;

    static {
        VALID_VALUES_STRING_ARRAY = Arrays.stream(IoTProtocol.values())
                .map(Enum::name)
                .toArray(String[]::new);
    }

    public boolean isSecure() {
        return this == MQTT_TLS;
    }
}
