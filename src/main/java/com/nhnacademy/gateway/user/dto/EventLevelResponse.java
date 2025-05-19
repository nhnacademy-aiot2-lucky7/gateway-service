package com.nhnacademy.gateway.user.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@SuppressWarnings("unused")
public class EventLevelResponse {
    private String eventLevelName;

    private String eventLevelDetails;

    private Integer priority;
}