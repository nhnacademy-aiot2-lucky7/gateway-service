package com.nhnacademy.gateway.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public final class SensorDataDetail {

    private final Long sensorNo;

    private final Long gatewayId;

    private final String sensorId;

    private final String sensorName;

    private final String typeEnName;

    private final String sensorLocation;

    private final String sensorSpot;

    private final String status;

    @JsonCreator
    public SensorDataDetail(
            @JsonProperty("sensor_data_no") Long sensorNo,
            @JsonProperty("gateway_id") Long gatewayId,
            @JsonProperty("sensor_id") String sensorId,
            @JsonProperty("sensor_name") String sensorName,
            @JsonProperty("type_en_name") String typeEnName,
            @JsonProperty("location") String sensorLocation,
            @JsonProperty("spot") String sensorSpot,
            @JsonProperty("status") String status
    ) {
        this.sensorNo = sensorNo;
        this.gatewayId = gatewayId;
        this.sensorId = sensorId;
        this.sensorName = sensorName;
        this.typeEnName = typeEnName;
        this.sensorLocation = sensorLocation;
        this.sensorSpot = sensorSpot;
        this.status = status;
    }
}
