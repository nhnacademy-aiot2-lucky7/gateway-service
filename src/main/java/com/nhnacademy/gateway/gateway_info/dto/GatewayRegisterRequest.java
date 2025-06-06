package com.nhnacademy.gateway.gateway_info.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.nhnacademy.gateway.common.enums.IoTProtocol;
import com.nhnacademy.gateway.common.jackson.IoTProtocolDeserializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public final class GatewayRegisterRequest implements GatewayRequest {

    @NotBlank
    private final String address;

    @NotNull
    private final Integer port;

    @NotNull
    private final IoTProtocol protocol;

    @NotBlank
    private final String gatewayName;

    @NotBlank
    private final String departmentId;

    @NotBlank
    private final String description;

    @JsonCreator
    public GatewayRegisterRequest(
            @JsonProperty("address") String address,
            @JsonProperty("port") Integer port,
            @JsonProperty("protocol")
            @JsonDeserialize(using = IoTProtocolDeserializer.class) IoTProtocol protocol,
            @JsonProperty("gateway_name") String gatewayName,
            @JsonProperty("department_id") String departmentId,
            @JsonProperty("description") String description
    ) {
        this.address = address;
        this.port = port;
        this.protocol = protocol;
        this.gatewayName = gatewayName;
        this.departmentId = departmentId;
        this.description = description;
    }
}
