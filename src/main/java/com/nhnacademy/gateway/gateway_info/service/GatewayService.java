package com.nhnacademy.gateway.gateway_info.service;

import com.nhnacademy.gateway.gateway_info.domain.Gateway;
import com.nhnacademy.gateway.gateway_info.dto.GatewayAdminSummaryResponse;
import com.nhnacademy.gateway.gateway_info.dto.GatewayDataDetailResponse;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRegisterRequest;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRequest;
import com.nhnacademy.gateway.gateway_info.dto.GatewaySummaryResponse;
import com.nhnacademy.gateway.gateway_info.dto.GatewayUpdateRequest;

import java.util.List;

public interface GatewayService {

    String[] getSupportedProtocols();

    long registerGateway(GatewayRegisterRequest request);

    Gateway getGatewayByGatewayId(long gatewayId);

    void updateGatewayInfo(GatewayUpdateRequest request);

    void updateSensorCountByGatewayId(long gatewayId, int sensorCount);

    void updateThresholdStatusEnabledByGatewayId(Long gatewayId);

    boolean isExistsGatewayId(long gatewayId);

    boolean isExistsGateway(GatewayRequest request);

    String getDepartmentIdByGatewayId(long gatewayId);

    List<Long> getGatewayIds();

    List<GatewaySummaryResponse> getGatewaySummariesByDepartmentId(String departmentId);

    List<GatewayAdminSummaryResponse> getGatewayAdminSummaries();

    GatewayDataDetailResponse getGatewayDetailsByGatewayId(long gatewayId);
}
