package com.nhnacademy.gateway.gateway_info.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nhnacademy.gateway.common.enums.IoTProtocol;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public final class GatewayInfoResponse {

    @JsonProperty("gateway_id")
    private final Long gatewayId;

    @JsonProperty("address")
    private final String gatewayAddress;

    @JsonProperty("port")
    private final Integer gatewayPort;

    @JsonProperty("protocol")
    private final IoTProtocol ioTProtocol;

    @JsonProperty("gateway_name")
    private final String gatewayName;

    @JsonProperty("client_id")
    private final String clientId;

    @JsonProperty("department_id")
    private final String departmentId;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private final LocalDateTime updatedAt;

    @JsonProperty("threshold_status")
    private final Boolean thresholdStatus;

    @QueryProjection
    public GatewayInfoResponse(
            Long gatewayId, String gatewayAddress, Integer gatewayPort,
            IoTProtocol ioTProtocol, String gatewayName, String clientId,
            String departmentId, String description,
            LocalDateTime createdAt, LocalDateTime updatedAt,
            Boolean thresholdStatus
    ) {
        this.gatewayId = gatewayId;
        this.gatewayAddress = gatewayAddress;
        this.gatewayPort = gatewayPort;
        this.ioTProtocol = ioTProtocol;
        this.gatewayName = gatewayName;
        this.clientId = clientId;
        this.departmentId = departmentId;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.thresholdStatus = thresholdStatus;
    }
}
