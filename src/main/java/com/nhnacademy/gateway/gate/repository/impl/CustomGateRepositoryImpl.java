package com.nhnacademy.gateway.gate.repository.impl;

import com.nhnacademy.gateway.gate.domain.QGate;
import com.nhnacademy.gateway.gate.dto.GateSummaryResponse;
import com.nhnacademy.gateway.gate.repository.CustomGateRepository;
import com.querydsl.core.types.Projections;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;

@Repository
public class CustomGateRepositoryImpl implements CustomGateRepository {

    private final JPAQueryFactory queryFactory;

    public CustomGateRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<GateSummaryResponse> findGateSummaries() {
        QGate gate = QGate.gate;

        return queryFactory
                .select(Projections.constructor(
                        GateSummaryResponse.class,
                        gate.gateNo,
                        gate.gateName,
                        gate.protocol,
                        gate.isActive,
                        gate.thresholdStatus
                ))
                .from(gate)
                .fetch();
    }

}
