package com.nhnacademy.gateway.gate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GateSummaryResponse {
    private Long gateNo;
    private String gateName;
    private String protocol;
    private boolean isActive;
    private boolean thresholdStatus;
}