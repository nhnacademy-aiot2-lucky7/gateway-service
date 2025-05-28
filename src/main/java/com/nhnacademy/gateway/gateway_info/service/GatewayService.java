package com.nhnacademy.gateway.gateway_info.service;

import com.nhnacademy.gateway.gateway_info.dto.GatewayRegisterRequest;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRequest;

public interface GatewayService {

    String[] getSupportedProtocols();

    int registerGateway(GatewayRegisterRequest request);

    boolean isExistsGateway(GatewayRequest request);
}
