package com.nhnacademy.gateway.gate.repository;

import com.nhnacademy.gateway.gate.domain.Gate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GateRepositoryTest {

    @Autowired
    private GateRepository gateRepository;

    @Test
    @DisplayName("IP와 Port로 존재 여부를 확인했을 때 존재하면 true 반환")
    void existsByBrokerIpAndPort_ReturnsTrue_WhenExists() {

        Gate gate = Gate.ofNewGate(
                "TestGate",
                "MQTT",
                "192.168.0.1",
                1883,
                "D001",
                "테스트 게이트웨이"
        );
        gateRepository.save(gate);

        boolean exists = gateRepository.existsByBrokerIpAndPort("192.168.0.1", 1883);

        assertTrue(exists);
    }

    @Test
    @DisplayName("IP와 Port로 존재 여부를 확인했을 때 존재하지 않으면 false 반환")
    void existsByBrokerIpAndPort_ReturnsFalse_WhenNotExists() {

        boolean exists = gateRepository.existsByBrokerIpAndPort("10.0.0.1", 1234);

        assertFalse(exists);
    }
}
