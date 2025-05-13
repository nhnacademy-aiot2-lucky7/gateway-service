package com.nhnacademy.gateway.gate.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@SuppressWarnings("unused")
public class GateSummaryResponse {
    private Long gateNo;
    private String gateName;
    private String protocol;
    private boolean isActive;
    private boolean thresholdStatus;
}