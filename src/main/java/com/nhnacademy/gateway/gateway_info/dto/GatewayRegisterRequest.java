package com.nhnacademy.gateway.gateway_info.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nhnacademy.gateway.common.enums.IoTProtocol;
import lombok.Getter;

@Getter
public final class GatewayRegisterRequest implements GatewayRequest {

    private final String address;

    private final Integer port;

    private final IoTProtocol protocol;

    private final String gatewayName;

    private final String departmentId;

    private final String description;

    @JsonCreator
    public GatewayRegisterRequest(
            @JsonProperty("address") String address,
            @JsonProperty("port") Integer port,
            @JsonProperty("protocol") IoTProtocol protocol,
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
