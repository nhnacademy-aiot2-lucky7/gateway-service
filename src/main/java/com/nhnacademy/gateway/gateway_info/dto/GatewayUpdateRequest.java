package com.nhnacademy.gateway.gateway_info.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/// TODO: validation 메세지 설정할 예정...
@Getter
public final class GatewayUpdateRequest {

    @NotNull
    @Positive
    private final Long gatewayId;

    @NotBlank
    @Size(min = 1, max = 50)
    private final String gatewayName;

    @NotBlank
    private final String description;

    @JsonCreator
    public GatewayUpdateRequest(
            @JsonProperty("gateway_id") Long gatewayId,
            @JsonProperty("gateway_name") String gatewayName,
            @JsonProperty("description") String description
    ) {
        this.gatewayId = gatewayId;
        this.gatewayName = gatewayName;
        this.description = description;
    }
}
