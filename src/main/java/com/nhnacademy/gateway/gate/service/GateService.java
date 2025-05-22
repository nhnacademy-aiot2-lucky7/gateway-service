package com.nhnacademy.gateway.gate.service;

import com.nhnacademy.gateway.gate.dto.GateRequest;
import com.nhnacademy.gateway.gate.dto.GateResponse;
import com.nhnacademy.gateway.gate.dto.GateSummaryResponse;

import java.util.List;

public interface GateService {

    Long createGate(GateRequest gateRegisterRequest);

    GateResponse getGate(Long gateNo);

    GateResponse getGateByAddress(String ip, int port);

    List<GateSummaryResponse> getGateList();

    boolean updateGate(Long gateNo, GateRequest gateUpdateRequest);

    void changeActivate(Long gateNo);

    void changeInactivate(Long gateNo);

    void changeThresholdStatus(Long gateNo);

    void deleteGate(Long gateNo);
}