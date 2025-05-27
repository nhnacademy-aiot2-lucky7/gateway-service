package com.nhnacademy.gateway.gateway_info.service.impl;

import com.nhnacademy.gateway.common.enums.IoTProtocol;
import com.nhnacademy.gateway.common.exception.http.extend.GatewayAlreadyExistsException;
import com.nhnacademy.gateway.gateway_info.domain.Gateway;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRegisterRequest;
import com.nhnacademy.gateway.gateway_info.dto.GatewayRequest;
import com.nhnacademy.gateway.gateway_info.repository.GatewayRepository;
import com.nhnacademy.gateway.gateway_info.service.GatewayService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class GatewayServiceImpl implements GatewayService {

    private final GatewayRepository gatewayRepository;

    public GatewayServiceImpl(GatewayRepository gatewayRepository) {
        this.gatewayRepository = gatewayRepository;
    }

    @Override
    public List<String> getSupportedProtocols() {
        return Arrays.stream(IoTProtocol.values())
                .map(Enum::name)
                .toList();
    }

    @Override
    public int registerGateway(GatewayRegisterRequest request) {
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

    @Override
    public boolean isExistsGateway(GatewayRequest request) {
        return gatewayRepository.existsGatewayByAddressAndPort(
                request.getAddress(),
                request.getPort()
        );
    }
}
