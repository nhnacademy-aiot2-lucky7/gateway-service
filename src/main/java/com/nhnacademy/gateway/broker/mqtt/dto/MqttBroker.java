package com.nhnacademy.gateway.broker.mqtt.dto;

import com.nhnacademy.gateway.common.enums.IoTProtocol;
import lombok.Getter;

@Getter
public abstract class MqttBroker {

    private final long gatewayNo;

    private final String address;

    private final int port;

    private final IoTProtocol protocol;

    private final String clientId;

    private final BrokerType brokerType;

    private final String topic = "data/#";

    protected MqttBroker(
            long gatewayNo, String address, int port,
            IoTProtocol protocol, String clientId,
            BrokerType brokerType
    ) {
        this.gatewayNo = gatewayNo;
        this.address = address;
        this.port = port;
        this.protocol = protocol;
        this.clientId = clientId;
        this.brokerType = brokerType;
    }

    public String getServerURI() {
        return "%s://%s:%d".formatted(
                getProtocolScheme(),
                address,
                port
        );
    }

    public String getBuildClientIdWithTimestamp() {
        return "%s_%d".formatted(
                clientId,
                System.currentTimeMillis()
        );
    }

    private String getProtocolScheme() {
        return protocol.isSecure() ? "ssl" : "tcp";
    }
}
