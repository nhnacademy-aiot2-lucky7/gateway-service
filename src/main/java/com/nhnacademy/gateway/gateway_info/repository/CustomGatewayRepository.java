package com.nhnacademy.gateway.gateway_info.repository;

import com.nhnacademy.gateway.broker.mqtt.dto.MqttBroker;

import java.util.List;

public interface CustomGatewayRepository {

    /**
     * SELECT department_id
     * FROM gateways
     * WHERE gateway_id = ?
     */
    String getDepartmentIdByGatewayId(long gatewayId);

    List<MqttBroker> getMqttBrokers();
}
