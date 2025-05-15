package com.nhnacademy.gateway.gate.controller;

import com.nhnacademy.gateway.gate.dto.GateRequest;
import com.nhnacademy.gateway.gate.dto.GateResponse;
import com.nhnacademy.gateway.gate.dto.GateSummaryResponse;
import com.nhnacademy.gateway.gate.service.GateService;
import com.nhnacademy.gateway.mqtt.client.GatewayConnector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gates")
@RequiredArgsConstructor
@Slf4j
public class GateController {

    private final GateService gateService;
    private final GatewayConnector connector;

    @PostMapping("/connect")
    public ResponseEntity<Long> createGate(@Validated @RequestBody GateRequest gateRequest) {

        // 1. DB 저장 (isActive = false 상태)
        Long gateNo = gateService.createGate(gateRequest);

        // 2. MQTT 연결 시도
        String gateUrl = "tcp://" + gateRequest.getBrokerIp() + ":" + gateRequest.getPort();
        connector.startGateway(gateUrl, gateNo.toString(), gateRequest.getProtocol());

        // 3. isActive = true 로 변경
        gateService.changeActivate(gateNo);

        return ResponseEntity.status(HttpStatus.CREATED).body(gateNo);
    }

    @GetMapping("/{gateNo}")
    public ResponseEntity<GateResponse> getGate(@PathVariable Long gateNo) {

        GateResponse response = gateService.getGate(gateNo);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<List<GateSummaryResponse>> getGateList() {

        List<GateSummaryResponse> gateList = gateService.getGateList();

        if (gateList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(gateList);
    }

    @PutMapping("/{gateNo}")
    public ResponseEntity<Void> updateGate(@PathVariable Long gateNo, @Validated @RequestBody GateRequest gateRequest) {

        // 연결 재시도가 필요한지 확인
        boolean resetNeeded = gateService.updateGate(gateNo, gateRequest);

        if (resetNeeded) {
            // 기존 연결 종료
            connector.shutdown(gateNo.toString());

            // 새 연결 시도
            String brokerUrl = "tcp://" + gateRequest.getBrokerIp() + ":" + gateRequest.getPort();
            connector.startGateway(brokerUrl, gateNo.toString(), gateRequest.getProtocol());
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{gateNo}")
    public ResponseEntity<Void> deleteGate(@PathVariable Long gateNo) {

        // 연결 종료
        connector.shutdown(gateNo.toString());

        // DB 삭제
        gateService.deleteGate(gateNo);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{gateNo}/activate")
    public ResponseEntity<Void> changeActivate(@PathVariable Long gateNo) {

        // MQTT 연결
        GateResponse gate = gateService.getGate(gateNo);
        String gateUrl = "tcp://" + gate.getBrokerIp() + ":" + gate.getPort();

        connector.startGateway(gateUrl, gateNo.toString(), gate.getProtocol());

        gateService.changeActivate(gateNo);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{gateNo}/inactivate")
    public ResponseEntity<Void> changeInactivate(@PathVariable Long gateNo) {

        // 연결 종료
        connector.shutdown(gateNo.toString());

        gateService.changeInactivate(gateNo);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{gateNo}/threshold")
    public ResponseEntity<Void> changeThresholdStatus(@PathVariable Long gateNo) {

        gateService.changeThresholdStatus(gateNo);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{gateNo}/department")
    public ResponseEntity<String> getDepartmentId(@PathVariable Long gateNo) {

        GateResponse gate = gateService.getGate(gateNo);

        return ResponseEntity.ok(gate.getDepartmentId());
    }
}
