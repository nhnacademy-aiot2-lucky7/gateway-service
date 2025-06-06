package com.nhnacademy.gateway.gateway_info.dto;

import com.nhnacademy.gateway.infrastructure.dto.SensorDataDetailResponse;
import lombok.Getter;

import java.util.List;

@Getter
public final class GatewayDataDetailResponse {

    private final GatewayDetailResponse gateway;

    private final List<SensorDataDetailResponse> sensors;

    public GatewayDataDetailResponse(
            GatewayDetailResponse gateway,
            List<SensorDataDetailResponse> sensors
    ) {
        this.gateway = gateway;
        this.sensors = sensors;
    }
}
