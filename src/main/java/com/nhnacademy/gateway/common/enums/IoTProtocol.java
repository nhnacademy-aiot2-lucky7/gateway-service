package com.nhnacademy.gateway.common.enums;

import java.util.Arrays;
import java.util.List;

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

    public static final String[] VALID_VALUES_STRING_ARRAY;

    public static final List<String> VALID_VALUES_STRING_LIST;

    static {
        VALID_VALUES_STRING_LIST = Arrays.stream(IoTProtocol.values())
                .map(Enum::name)
                .toList();
        VALID_VALUES_STRING_ARRAY = VALID_VALUES_STRING_LIST.toArray(String[]::new);
    }

    public boolean isSecure() {
        return this == MQTT_TLS;
    }
}
