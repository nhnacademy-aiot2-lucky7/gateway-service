package com.nhnacademy.gateway.gate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GateResponse {

    @JsonProperty("gateNo")
    private Long gateNo;

    @JsonProperty("gateName")
    private String gateName;

    @JsonProperty("protocol")
    private String protocol;

    @JsonProperty("brokerIp")
    private String brokerIp;

    @JsonProperty("port")
    private Integer port;

    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("departmentId")
    private String departmentId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("isActive")
    private boolean isActive;

    @JsonProperty("thresholdStatus")
    private boolean thresholdStatus;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}