package com.nhnacademy.gateway.gate.repository;

import com.nhnacademy.gateway.gate.dto.GateSummaryResponse;

import java.util.List;

public interface CustomGateRepository {
    List<GateSummaryResponse> findGateSummaries();
}