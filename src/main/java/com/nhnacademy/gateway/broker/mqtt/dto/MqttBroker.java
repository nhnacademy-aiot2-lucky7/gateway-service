package com.nhnacademy.gateway.broker.mqtt.dto;

import com.nhnacademy.gateway.common.enums.IoTProtocol;
import lombok.Getter;

@Getter
public abstract class MqttBroker {

    private final long gatewayId;

    private final String address;

    private final int port;

    private final IoTProtocol protocol;

    private final String clientId;

    private final String topic;

    private final int qos;

    private final BrokerType brokerType;

    protected MqttBroker(
            long gatewayId, String address, int port, IoTProtocol protocol,
            String clientId, String topic, int qos, BrokerType brokerType
    ) {
        this.gatewayId = gatewayId;
        this.address = address;
        this.port = port;
        this.protocol = protocol;
        this.clientId = clientId;
        this.topic = topic;
        this.qos = qos;
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
