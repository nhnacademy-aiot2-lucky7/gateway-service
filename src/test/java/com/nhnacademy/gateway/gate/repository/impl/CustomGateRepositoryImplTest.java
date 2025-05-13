package com.nhnacademy.gateway.gate.repository.impl;

import com.nhnacademy.gateway.gate.domain.Gate;
import com.nhnacademy.gateway.gate.dto.GateSummaryResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomGateRepositoryImplTest {

    @Autowired
    private EntityManager entityManager;

    private CustomGateRepositoryImpl customGateRepository;

    @BeforeEach
    void setUp() {
        customGateRepository = new CustomGateRepositoryImpl(entityManager);
    }

    @Test
    @DisplayName("findGateSummaries(): 게이트 요약 정보 조회가 정상적으로 동작해야 함")
    void findGateSummaries_ShouldReturnSummaryList() {

        Gate gate = Gate.ofNewGate(
                "TestGate",
                "MQTT",
                "192.168.0.1",
                1883,
                "D001",
                "테스트 설명"
        );
        gate.changeIsActive(true);
        gate.changeThresholdStatus(false);
        entityManager.persist(gate);
        entityManager.flush();
        entityManager.clear();

        List<GateSummaryResponse> result = customGateRepository.findGateSummaries();

        assertEquals(1, result.size());
        GateSummaryResponse summary = result.get(0);
        assertEquals("TestGate", summary.getGateName());
        assertEquals("MQTT", summary.getProtocol());
        assertTrue(summary.isActive());
        assertFalse(summary.isThresholdStatus());
    }
}