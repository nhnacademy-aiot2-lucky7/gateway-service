package com.nhnacademy.gateway.gate.controller;

import com.nhnacademy.gateway.gate.dto.GateRequest;
import com.nhnacademy.gateway.gate.dto.GateResponse;
import com.nhnacademy.gateway.gate.service.GateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gates")
@RequiredArgsConstructor
@Slf4j
public class GateController {

    private final GateService gateService;

    @PostMapping("/connect")
    public ResponseEntity<Long> createGate(@RequestHeader("X-User-Id") String encryptedEmail, @Validated @RequestBody GateRequest gateRegisterRequest) {

        Long gateNo = gateService.createGate(encryptedEmail, gateRegisterRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(gateNo);
    }

    @GetMapping("/{gateNo}")
    public ResponseEntity<GateResponse> getGate(@PathVariable Long gateNo) {

        GateResponse gateResponse = gateService.getGate(gateNo);

        return ResponseEntity
                .ok(gateResponse);
    }

    @PutMapping("/{gateNo}")
    public ResponseEntity<Void> updateGate(@PathVariable Long gateNo, @Validated @RequestBody GateRequest gateUpdateRequest) {

        gateService.updateGate(gateNo, gateUpdateRequest);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{gateNo}")
    public ResponseEntity<Void> deleteGate(@PathVariable Long gateNo) {

        gateService.deleteGate(gateNo);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{gateNo}/activate")
    public ResponseEntity<Void> changeIsActivate(@PathVariable Long gateNo) {

        gateService.changeActivate(gateNo);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{gateNo}/threshold")
    public ResponseEntity<Void> changeThresholdStatus(@PathVariable Long gateNo) {

        gateService.changeThresholdStatus(gateNo);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{gateNo}/department")
    public ResponseEntity<String> getDepartmentId(@PathVariable Long gateNo) {

        GateResponse gateResponse = gateService.getGate(gateNo);

        return ResponseEntity.ok(gateResponse.getDepartmentId());
    }
}