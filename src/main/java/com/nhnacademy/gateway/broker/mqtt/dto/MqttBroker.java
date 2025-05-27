package com.nhnacademy.gateway.broker.mqtt.dto;

import com.nhnacademy.gateway.common.enums.IoTProtocol;
import lombok.Getter;

@Getter
public abstract class MqttBroker {

    private final String address;

    private final int port;

    private final IoTProtocol protocol;

    private final String clientId;

    protected MqttBroker(String address, int port, IoTProtocol protocol, String clientId) {
        this.address = address;
        this.port = port;
        this.protocol = protocol;
        this.clientId = clientId;
    }

    public String getServerURI() {
        return "%s://%s:%d".formatted(
                getProtocolScheme(),
                address,
                port
        );
    }

    public String buildClientIdWithTimestamp() {
        return "%s_%d".formatted(
                clientId,
                System.currentTimeMillis()
        );
    }

    private String getProtocolScheme() {
        return protocol.isSecure() ? "ssl" : "tcp";
    }
}
