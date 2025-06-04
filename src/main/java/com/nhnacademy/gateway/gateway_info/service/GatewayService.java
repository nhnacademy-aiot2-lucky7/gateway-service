package com.nhnacademy.gateway.gateway_info.service;

import com.nhnacademy.gateway.gateway_info.dto.GatewayInfoResponse;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRegisterRequest;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRequest;

import java.util.List;

public interface GatewayService {

    String[] getSupportedProtocols();

    long registerGateway(GatewayRegisterRequest request);

    boolean isExistsGateway(GatewayRequest request);

    String getDepartmentIdByGatewayId(long gatewayId);

    List<Long> getGatewayIds();

    List<GatewayInfoResponse> getGateways(String departmentId);
}
