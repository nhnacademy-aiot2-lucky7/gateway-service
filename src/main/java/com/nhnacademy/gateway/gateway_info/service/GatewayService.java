package com.nhnacademy.gateway.gateway_info.service;

import com.nhnacademy.gateway.gateway_info.dto.GatewayRegisterRequest;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRequest;
import com.nhnacademy.gateway.gateway_info.dto.GatewayWebResponse;

import java.util.List;

public interface GatewayService {

    String[] getSupportedProtocols();

    long registerGateway(GatewayRegisterRequest request);

    boolean isExistsGateway(GatewayRequest request);

    String getDepartmentIdByGatewayId(long gatewayId);

    List<Long> getGatewayIds();

    List<GatewayWebResponse> getWebGatewaysByDepartmentId(String departmentId);
}
