package com.nhnacademy.gateway.broker.mqtt.dto;

import com.nhnacademy.gateway.broker.BrokerType;
import com.nhnacademy.gateway.common.enums.IoTProtocol;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public final class MqttInboundBroker extends MqttBroker {

    @QueryProjection
    public MqttInboundBroker(
            long gatewayId, String address, int port,
            IoTProtocol protocol, String clientId
    ) {
        super(
                gatewayId,
                address,
                port,
                protocol,
                clientId,
                "data/#",
                1,
                BrokerType.INBOUND
        );
    }
}
