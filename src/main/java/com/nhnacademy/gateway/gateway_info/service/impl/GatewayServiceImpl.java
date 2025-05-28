package com.nhnacademy.gateway.gateway_info.service.impl;

import com.nhnacademy.gateway.common.enums.IoTProtocol;
import com.nhnacademy.gateway.common.exception.http.extend.GatewayAlreadyExistsException;
import com.nhnacademy.gateway.gateway_info.domain.Gateway;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRegisterRequest;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRequest;
import com.nhnacademy.gateway.gateway_info.repository.GatewayRepository;
import com.nhnacademy.gateway.gateway_info.service.GatewayService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
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
                false
        );
        return gatewayRepository.save(gateway)
                .getGatewayNo();
    }

    public boolean isExistsGatewayNo(long gatewayNo) {
        return gatewayRepository.existsById(gatewayNo);
    }

    @Override
    public boolean isExistsGateway(GatewayRequest request) {
        return gatewayRepository.existsGatewayByAddressAndPort(
                request.getAddress(),
                request.getPort()
        );
    }

    /*@Override
    public String getDepartmentIdByGatewayNo(long gatewayNo) {
        if (!isExistsGatewayNo(gatewayNo)) {
            throw new GatewayNotFoundException();
        }
        return gatewayRepository.getDepartmentIdByGatewayNo(gatewayNo);
    }*/
}
