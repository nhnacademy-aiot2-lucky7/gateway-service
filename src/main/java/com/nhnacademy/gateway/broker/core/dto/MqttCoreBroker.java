package com.nhnacademy.gateway.broker.core.dto;

import com.nhnacademy.gateway.broker.BrokerType;
import com.nhnacademy.gateway.broker.mqtt.dto.MqttBroker;
import com.nhnacademy.gateway.common.enums.IoTProtocol;
import com.nhnacademy.gateway.common.properties.CoreBrokerProperties;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public final class MqttCoreBroker extends MqttBroker {

    public MqttCoreBroker(CoreBrokerProperties properties) {
        super(
                0L,
                properties.getAddress(),
                properties.getPort(),
                properties.isSecure() ? IoTProtocol.MQTT_TLS : IoTProtocol.MQTT,
                properties.getClientId(),
                properties.getTopic(),
                properties.getQos(),
                BrokerType.CORE
        );
    }
}
