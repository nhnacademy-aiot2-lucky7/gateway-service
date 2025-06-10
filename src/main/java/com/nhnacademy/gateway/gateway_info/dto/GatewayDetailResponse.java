package com.nhnacademy.gateway.gateway_info.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nhnacademy.gateway.common.enums.IoTProtocol;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GatewayDetailResponse {

    private Long gatewayId;

    private String address;

    private Integer port;

    @JsonProperty("iot_protocol")
    private IoTProtocol protocol;

    private String gatewayName;

    private String clientId;

    private String departmentId;

    private String description;

    private Integer sensorCount;

    private Boolean thresholdStatus;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy.MM.dd"
    )
    private LocalDateTime createdAt;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy.MM.dd"
    )
    private LocalDateTime updatedAt;

    @QueryProjection
    public GatewayDetailResponse(
            Long gatewayId, String address, Integer port, IoTProtocol protocol,
            String gatewayName, String clientId, String departmentId,
            String description, Integer sensorCount, Boolean thresholdStatus,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        this.gatewayId = gatewayId;
        this.address = address;
        this.port = port;
        this.protocol = protocol;
        this.gatewayName = gatewayName;
        this.clientId = clientId;
        this.departmentId = departmentId;
        this.description = description;
        this.sensorCount = sensorCount;
        this.thresholdStatus = thresholdStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
