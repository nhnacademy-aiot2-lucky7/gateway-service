package com.nhnacademy.gateway.gateway_info.dto;

import com.nhnacademy.gateway.infrastructure.dto.SensorDataDetail;
import lombok.Getter;

import java.util.List;

@Getter
public final class GatewayDataDetailResponse {

    private final GatewayDetailResponse gateway;

    private final List<SensorDataDetail> sensors;

    public GatewayDataDetailResponse(
            GatewayDetailResponse gateway,
            List<SensorDataDetail> sensors
    ) {
        this.gateway = gateway;
        this.sensors = sensors;
    }
}
