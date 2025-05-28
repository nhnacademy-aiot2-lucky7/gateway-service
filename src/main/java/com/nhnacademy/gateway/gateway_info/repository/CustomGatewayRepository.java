package com.nhnacademy.gateway.gateway_info.repository;

import com.nhnacademy.gateway.broker.mqtt.dto.MqttBroker;

import java.util.List;

public interface CustomGatewayRepository {

    /**
     * SELECT department_id
     * FROM gateways
     * WHERE gateway_no = ?
     */
    String getDepartmentIdByGatewayNo(long gatewayNo);

    List<MqttBroker> getMqttBrokers();
}
