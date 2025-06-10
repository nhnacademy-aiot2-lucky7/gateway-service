package com.nhnacademy.gateway.infrastructure.adapter;

import com.nhnacademy.gateway.infrastructure.dto.SensorDataDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "sensor-service")
public interface SensorDataServiceAdapter {

    @GetMapping("/sensor-data-mappings/gateway-id/{gateway-id}/sensors")
    ResponseEntity<List<SensorDataDetail>> getSensorDataDetailsByGatewayId(
            @PathVariable("gateway-id") Long gatewayId
    );
}
