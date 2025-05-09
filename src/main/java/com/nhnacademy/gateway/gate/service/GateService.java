package com.nhnacademy.gateway.gate.service;

import com.nhnacademy.gateway.gate.dto.GateRequest;
import com.nhnacademy.gateway.gate.dto.GateResponse;
import com.nhnacademy.gateway.gate.dto.GateSummaryResponse;

import java.util.List;

public interface GateService {

    Long createGate(String encryptedEmail, GateRequest gateRegisterRequest);

    GateResponse getGate(Long gateNo);

    List<GateSummaryResponse> getGateList();

    void updateGate(Long gateNo, GateRequest gateUpdateRequest);

    void changeActivate(Long gateNo);

    void changeInactivate(Long gateNo);

    void changeThresholdStatus(Long gateNo);

    void deleteGate(Long gateNo);
}