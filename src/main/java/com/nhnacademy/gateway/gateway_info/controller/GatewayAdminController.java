package com.nhnacademy.gateway.gateway_info.controller;

import com.nhnacademy.gateway.common.annotation.CheckRole;
import com.nhnacademy.gateway.common.enums.RoleType;
import com.nhnacademy.gateway.gateway_info.dto.GatewayAdminSummaryResponse;
import com.nhnacademy.gateway.gateway_info.service.GatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CheckRole(RoleType.ROLE_ADMIN)
@RestController
@RequestMapping("/admin/gateways")
public class GatewayAdminController {

    private final GatewayService gatewayService;

    public GatewayAdminController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @GetMapping
    public ResponseEntity<List<GatewayAdminSummaryResponse>> getGatewayAdminSummaries() {
        return ResponseEntity
                .ok(gatewayService.getGatewayAdminSummaries());
    }
}
