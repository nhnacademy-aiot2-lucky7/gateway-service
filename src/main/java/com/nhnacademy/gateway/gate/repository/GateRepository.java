package com.nhnacademy.gateway.gate.repository;

import com.nhnacademy.gateway.gate.domain.Gate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GateRepository extends JpaRepository<Gate, Long>, CustomGateRepository {
    boolean existsByBrokerIpAndPort(String brokerIp, Integer port);
}