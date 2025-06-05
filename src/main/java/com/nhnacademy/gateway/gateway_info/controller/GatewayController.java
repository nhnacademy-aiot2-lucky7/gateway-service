package com.nhnacademy.gateway.gateway_info.controller;

import com.nhnacademy.gateway.gateway_info.dto.GatewayRegisterRequest;
import com.nhnacademy.gateway.gateway_info.dto.GatewayWebResponse;
import com.nhnacademy.gateway.gateway_info.service.GatewayService;
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

@RestController
@RequestMapping("/gateways")
public class GatewayController {

    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @GetMapping
    public ResponseEntity<List<GatewayWebResponse>> getWebGatewaysByDepartmentId(
            @RequestBody String departmentId
    ) {
        return ResponseEntity
                .ok(gatewayService.getWebGatewaysByDepartmentId(departmentId));
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

    @PutMapping("/update-count")
    public ResponseEntity<Void> updateGatewaySensorCount(

    ) {
        return ResponseEntity
                .noContent()
                .build();
    }
}
