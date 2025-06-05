package com.nhnacademy.gateway.gateway_info.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public final class GatewayCountUpdateRequest {

    @NotNull
    private final Long gatewayId;

    @NotNull
    @PositiveOrZero
    private final Integer sensorCount;

    @JsonCreator
    public GatewayCountUpdateRequest(
            @JsonProperty("gateway_id") Long gatewayId,
            @JsonProperty("sensor_count") Integer sensorCount
    ) {
        this.gatewayId = gatewayId;
        this.sensorCount = sensorCount;
    }
}
