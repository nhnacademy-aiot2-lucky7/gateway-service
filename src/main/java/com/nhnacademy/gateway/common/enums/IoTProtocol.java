package com.nhnacademy.gateway.common.enums;

import com.nhnacademy.gateway.common.exception.http.BadRequestException;

import java.util.Arrays;

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

    public static final String VALID_VALUES_STRING;

    public static final String[] VALID_VALUES_STRING_ARRAY;

    static {
        VALID_VALUES_STRING_ARRAY = Arrays.stream(IoTProtocol.values())
                .map(Enum::name)
                .toArray(String[]::new);
        VALID_VALUES_STRING = String.join(", ", VALID_VALUES_STRING_ARRAY);
    }

    public static IoTProtocol from(String name) {
        for (IoTProtocol protocol : values()) {
            if (protocol.name().equalsIgnoreCase(name)) {
                return protocol;
            }
        }
        throw new BadRequestException(
                "'protocol'에 사용 가능한 값 [%s]"
                        .formatted(VALID_VALUES_STRING)
        );
    }

    public boolean isSecure() {
        return this == MQTT_TLS;
    }
}
