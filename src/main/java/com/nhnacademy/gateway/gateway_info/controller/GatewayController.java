package com.nhnacademy.gateway.gateway_info.controller;

import com.nhnacademy.gateway.gateway_info.dto.GatewayRegisterRequest;
import com.nhnacademy.gateway.gateway_info.service.GatewayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateways")
public class GatewayController {

    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @GetMapping("/supported-protocols")
    public ResponseEntity<String[]> getSupportedProtocols() {
        return ResponseEntity
                .ok(gatewayService.getSupportedProtocols());
    }

    @PostMapping
    public ResponseEntity<Long> registerGateway(
            @Validated @RequestBody GatewayRegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(gatewayService.registerGateway(request));
    }
}
