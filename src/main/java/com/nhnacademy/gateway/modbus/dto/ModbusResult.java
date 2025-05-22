package com.nhnacademy.gateway.modbus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ModbusResult {

    @JsonProperty("location")
    private String location;

    @JsonProperty("deviceName")
    private String deviceName;

    @JsonProperty("current")
    private double current;

    @JsonProperty("voltage")
    private double voltage;

    @JsonProperty("power")
    private double power;

    @JsonProperty("energy")
    private double energy;

    @JsonProperty("timestamp")
    private long timestamp;
}