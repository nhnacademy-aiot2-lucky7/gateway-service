package com.nhnacademy.gateway.gateway_info.repository;

import com.nhnacademy.gateway.broker.mqtt.dto.MqttBroker;
import com.nhnacademy.gateway.gateway_info.dto.GatewayInfoResponse;

import java.util.List;

public interface CustomGatewayRepository {

    /**
     * SELECT department_id
     * FROM gateways
     * WHERE gateway_id = ?
     */
    String getDepartmentIdByGatewayId(long gatewayId);

    List<Long> getGatewayIds();

    List<MqttBroker> getMqttBrokers();

    List<GatewayInfoResponse> getGateways(String departmentId);
}
