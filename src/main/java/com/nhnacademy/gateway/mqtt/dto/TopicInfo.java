package com.nhnacademy.gateway.mqtt.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class TopicInfo {

    String place;

    String deviceId;

    String position;

    String element;
}
