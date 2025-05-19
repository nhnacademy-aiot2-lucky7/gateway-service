package com.nhnacademy.gateway.mqtt.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class TopicInfo {

    private String place;

    private String type;

    private String deviceId;

    private String position;

    private String element;
}
