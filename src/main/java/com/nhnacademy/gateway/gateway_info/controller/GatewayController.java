package com.nhnacademy.gateway.gateway_info.controller;

import com.nhnacademy.gateway.gateway_info.dto.GatewayCountUpdateRequest;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRegisterRequest;
import com.nhnacademy.gateway.gateway_info.dto.GatewaySummaryResponse;
import com.nhnacademy.gateway.gateway_info.service.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/gateways")
public class GatewayController {

    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @GetMapping
    public ResponseEntity<List<GatewaySummaryResponse>> getGatewaySummariesByDepartmentId(
            @RequestBody String departmentId
    ) {
        return ResponseEntity
                .ok(gatewayService.getGatewaySummariesByDepartmentId(departmentId));
    }

    @GetMapping("/ids")
    public ResponseEntity<List<Long>> getGatewayIds() {
        return ResponseEntity
                .ok(gatewayService.getGatewayIds());
    }

    @GetMapping("/supported-protocols")
    public ResponseEntity<String[]> getSupportedProtocols() {
        return ResponseEntity
                .ok(gatewayService.getSupportedProtocols());
    }

    @GetMapping("/{gateway-id}/department-id")
    public ResponseEntity<String> getDepartment(
            @PathVariable("gateway-id") Long gatewayId
    ) {
        return ResponseEntity
                .ok(gatewayService.getDepartmentIdByGatewayId(gatewayId));
    }

    @PostMapping
    public ResponseEntity<Long> registerGateway(
            @Validated @RequestBody GatewayRegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(gatewayService.registerGateway(request));
    }

    /// TODO: Sensor-Service만 접근할 수 있도록 구조를 추가
    @PutMapping("/update-sensor-count")
    public ResponseEntity<Void> updateGatewaySensorCount(
            @Validated @RequestBody GatewayCountUpdateRequest request
    ) {
        gatewayService.updateSensorCountByGatewayId(
                request.getGatewayId(),
                request.getSensorCount()
        );
        return ResponseEntity
                .noContent()
                .build();
    }
}
