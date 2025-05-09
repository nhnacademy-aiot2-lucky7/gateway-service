package com.nhnacademy.gateway.gate.service;

import com.nhnacademy.gateway.gate.dto.GateRequest;
import com.nhnacademy.gateway.gate.dto.GateResponse;

public interface GateService {

    Long createGate(String encryptedEmail, GateRequest gateRegisterRequest);

    GateResponse getGate(Long gateNo);

    void updateGate(Long gateNo, GateRequest gateUpdateRequest);

    void changeActivate(Long gateNo);

    void changeInactivate(Long gateNo);

    void changeThresholdStatus(Long gateNo);

    void deleteGate(Long gateNo);
}