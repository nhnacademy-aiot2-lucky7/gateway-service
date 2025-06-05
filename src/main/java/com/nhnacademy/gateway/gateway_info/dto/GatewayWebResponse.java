package com.nhnacademy.gateway.gateway_info.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nhnacademy.gateway.common.enums.IoTProtocol;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public final class GatewayWebResponse {

    private final long gatewayId;

    private final String gatewayName;

    @JsonProperty("iot_protocol")
    private final IoTProtocol ioTProtocol;

    private final int sensorCount;

    private final boolean thresholdStatus;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy.MM.dd"
    )
    private final LocalDateTime updatedAt;

    @QueryProjection
    public GatewayWebResponse(
            long gatewayId, String gatewayName, IoTProtocol ioTProtocol,
            int sensorCount, boolean thresholdStatus, LocalDateTime updatedAt
    ) {
        this.gatewayId = gatewayId;
        this.gatewayName = gatewayName;
        this.ioTProtocol = ioTProtocol;
        this.sensorCount = sensorCount;
        this.thresholdStatus = thresholdStatus;
        this.updatedAt = updatedAt;
    }
}
