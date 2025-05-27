package com.nhnacademy.gateway.gateway_info.repository;

import com.nhnacademy.gateway.gateway_info.domain.Gateway;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatewayRepository extends JpaRepository<Gateway, Integer>, CustomGatewayRepository {

    boolean existsGatewayByIpAddressAndPort(String ipAddress, Integer port);
}
