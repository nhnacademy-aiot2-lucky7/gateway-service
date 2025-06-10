package com.nhnacademy.gateway.gateway_info.repository;

import com.nhnacademy.gateway.broker.mqtt.dto.MqttBroker;
import com.nhnacademy.gateway.gateway_info.dto.GatewayAdminSummaryResponse;
import com.nhnacademy.gateway.gateway_info.dto.GatewaySummaryResponse;
import com.nhnacademy.gateway.gateway_info.dto.GatewayDetailResponse;

import java.util.List;

public interface CustomGatewayRepository {

    /**
     * SELECT department_id
     * FROM gateways
     * WHERE gateway_id = ?
     */
    String getDepartmentIdByGatewayId(long gatewayId);

    List<Long> getGatewayIds();

    /**
     * Gateway-Service 가 내부적으로 사용하는 기능
     */
    List<MqttBroker> getMqttBrokers();

    List<GatewaySummaryResponse> findGatewaySummariesByDepartmentId(String departmentId);

    List<GatewayAdminSummaryResponse> findGatewayAdminSummaries();

    GatewayDetailResponse findGatewayDetailByGatewayId(long gatewayId);
}
