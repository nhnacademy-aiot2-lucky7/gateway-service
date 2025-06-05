package com.nhnacademy.gateway.gateway_info.service;

import com.nhnacademy.gateway.gateway_info.domain.Gateway;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRegisterRequest;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRequest;
import com.nhnacademy.gateway.gateway_info.dto.GatewaySummaryResponse;

import java.util.List;

public interface GatewayService {

    String[] getSupportedProtocols();

    long registerGateway(GatewayRegisterRequest request);

    Gateway getGatewayByGatewayId(long gatewayId);

    void updateSensorCountByGatewayId(long gatewayId, int sensorCount);

    boolean isExistsGateway(GatewayRequest request);

    String getDepartmentIdByGatewayId(long gatewayId);

    List<Long> getGatewayIds();

    List<GatewaySummaryResponse> getGatewaySummariesByDepartmentId(String departmentId);
}
