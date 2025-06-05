package com.nhnacademy.gateway.gateway_info.service.impl;

import com.nhnacademy.gateway.common.enums.IoTProtocol;
import com.nhnacademy.gateway.common.exception.http.extend.GatewayAlreadyExistsException;
import com.nhnacademy.gateway.common.exception.http.extend.GatewayNotFoundException;
import com.nhnacademy.gateway.gateway_info.domain.Gateway;
import com.nhnacademy.gateway.gateway_info.dto.GatewayAdminSummaryResponse;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRegisterRequest;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRequest;
import com.nhnacademy.gateway.gateway_info.dto.GatewaySummaryResponse;
import com.nhnacademy.gateway.gateway_info.repository.GatewayRepository;
import com.nhnacademy.gateway.gateway_info.service.GatewayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GatewayServiceImpl implements GatewayService {

    private final GatewayRepository gatewayRepository;

    public GatewayServiceImpl(GatewayRepository gatewayRepository) {
        this.gatewayRepository = gatewayRepository;
    }

    @Override
    public String[] getSupportedProtocols() {
        return IoTProtocol.VALID_VALUES_STRING_ARRAY;
    }

    @Override
    public long registerGateway(GatewayRegisterRequest request) {
        if (isExistsGateway(request)) {
            throw new GatewayAlreadyExistsException();
        }
        Gateway gateway = Gateway.ofNewGateway(
                request.getAddress(),
                request.getPort(),
                request.getProtocol(),
                request.getGatewayName(),
                UUID.randomUUID().toString(),
                request.getDepartmentId(),
                request.getDescription(),
                0,
                false
        );
        return gatewayRepository.save(gateway)
                .getGatewayId();
    }

    @Override
    public Gateway getGatewayByGatewayId(long gatewayId) {
        return gatewayRepository.findById(gatewayId)
                .orElseThrow(GatewayNotFoundException::new);
    }

    @Override
    public void updateSensorCountByGatewayId(long gatewayId, int sensorCount) {
        Gateway gateway = getGatewayByGatewayId(gatewayId);
        gateway.updateSensorCount(sensorCount);
        gatewayRepository.flush();
    }

    @Override
    public void updateThresholdStatusEnabledByGatewayId(Long gatewayId) {
        Gateway gateway = getGatewayByGatewayId(gatewayId);
        gateway.updateThresholdStatusEnabled();
        gatewayRepository.flush();
    }

    @Override
    public boolean isExistsGatewayId(long gatewayId) {
        return gatewayRepository.existsById(gatewayId);
    }

    @Override
    public boolean isExistsGateway(GatewayRequest request) {
        return gatewayRepository.existsGatewayByAddressAndPort(
                request.getAddress(),
                request.getPort()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public String getDepartmentIdByGatewayId(long gatewayId) {
        if (!isExistsGatewayId(gatewayId)) {
            throw new GatewayNotFoundException();
        }
        return gatewayRepository.getDepartmentIdByGatewayId(gatewayId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Long> getGatewayIds() {
        return gatewayRepository.getGatewayIds();
    }

    @Transactional(readOnly = true)
    @Override
    public List<GatewaySummaryResponse> getGatewaySummariesByDepartmentId(String departmentId) {
        return gatewayRepository.findGatewaySummariesByDepartmentId(departmentId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<GatewayAdminSummaryResponse> getGatewayAdminSummaries() {
        return gatewayRepository.findGatewayAdminSummaries();
    }
}
