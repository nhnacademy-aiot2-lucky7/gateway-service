package com.nhnacademy.gateway.gate.repository.impl;

import com.nhnacademy.gateway.gate.dto.GateSummaryResponse;
import com.nhnacademy.gateway.gate.repository.CustomGateRepository;
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
        return List.of();
    }

}
