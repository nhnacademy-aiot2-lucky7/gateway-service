package com.nhnacademy.gateway.gateway_info.service;

import com.nhnacademy.gateway.gateway_info.domain.Gateway;
import com.nhnacademy.gateway.gateway_info.dto.GatewayInfoResponse;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRegisterRequest;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRequest;

import java.util.List;

public interface GatewayService {

    String[] getSupportedProtocols();

    long registerGateway(GatewayRegisterRequest request);

    Gateway getGatewayByGatewayId(long gatewayId);

    void updateThresholdStatusEnabledByGatewayId(Long gatewayId);

    boolean isExistsGatewayId(long gatewayId);

    boolean isExistsGateway(GatewayRequest request);

    String getDepartmentIdByGatewayId(long gatewayId);

    List<Long> getGatewayIds();

    List<GatewayInfoResponse> getGateways(String departmentId);
}
