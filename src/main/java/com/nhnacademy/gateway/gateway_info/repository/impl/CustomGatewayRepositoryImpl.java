package com.nhnacademy.gateway.gateway_info.repository.impl;

import com.nhnacademy.gateway.broker.mqtt.dto.MqttBroker;
import com.nhnacademy.gateway.broker.mqtt.dto.QMqttInboundBroker;
import com.nhnacademy.gateway.common.enums.IoTProtocol;
import com.nhnacademy.gateway.gateway_info.domain.Gateway;
import com.nhnacademy.gateway.gateway_info.domain.QGateway;
import com.nhnacademy.gateway.gateway_info.dto.GatewayAdminSummaryResponse;
import com.nhnacademy.gateway.gateway_info.dto.GatewaySummaryResponse;
import com.nhnacademy.gateway.gateway_info.dto.QGatewayAdminSummaryResponse;
import com.nhnacademy.gateway.gateway_info.dto.QGatewaySummaryResponse;
import com.nhnacademy.gateway.gateway_info.repository.CustomGatewayRepository;
import com.nhnacademy.gateway.gateway_info.dto.GatewayDetailResponse;
import com.nhnacademy.gateway.infrastructure.dto.QGatewayDetailResponse;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.ArrayList;
import java.util.List;

public class CustomGatewayRepositoryImpl extends QuerydslRepositorySupport implements CustomGatewayRepository {

    private final JPAQueryFactory queryFactory;

    private final QGateway qGateway;

    public CustomGatewayRepositoryImpl(JPAQueryFactory queryFactory) {
        super(Gateway.class);
        this.queryFactory = queryFactory;
        this.qGateway = QGateway.gateway;
    }

    @Override
    public String getDepartmentIdByGatewayId(long gatewayId) {
        return queryFactory
                .select(qGateway.departmentId)
                .from(qGateway)
                .where(qGateway.gatewayId.eq(gatewayId))
                .fetchOne();
    }

    @Override
    public List<Long> getGatewayIds() {
        return queryFactory
                .select(qGateway.gatewayId)
                .from(qGateway)
                .fetch();
    }

    @Override
    public List<MqttBroker> getMqttBrokers() {
        return new ArrayList<>(
                queryFactory
                        .select(
                                new QMqttInboundBroker(
                                        qGateway.gatewayId,
                                        qGateway.address,
                                        qGateway.port,
                                        qGateway.protocol,
                                        qGateway.clientId
                                )
                        )
                        .from(qGateway)
                        .where(qGateway.protocol.eq(IoTProtocol.MQTT))
                        .fetch()
        );
    }

    @Override
    public List<GatewaySummaryResponse> findGatewaySummariesByDepartmentId(String departmentId) {
        return queryFactory
                .select(
                        new QGatewaySummaryResponse(
                                qGateway.gatewayId,
                                qGateway.gatewayName,
                                qGateway.protocol,
                                qGateway.sensorCount,
                                qGateway.thresholdStatus,
                                qGateway.updatedAt
                        )
                )
                .from(qGateway)
                .where(qGateway.departmentId.eq(departmentId))
                .orderBy(qGateway.createdAt.desc())
                .fetch();
    }

    @Override
    public List<GatewayAdminSummaryResponse> findGatewayAdminSummaries() {
        return queryFactory
                .select(
                        new QGatewayAdminSummaryResponse(
                                qGateway.gatewayId,
                                qGateway.gatewayName,
                                qGateway.protocol,
                                qGateway.departmentId,
                                qGateway.sensorCount,
                                qGateway.thresholdStatus,
                                qGateway.updatedAt
                        )
                )
                .from(qGateway)
                .orderBy(qGateway.createdAt.desc())
                .fetch();
    }

    @Override
    public GatewayDetailResponse findGatewayDetailByGatewayId(long gatewayId) {
        return queryFactory
                .select(
                        new QGatewayDetailResponse(
                                qGateway.gatewayId,
                                qGateway.address,
                                qGateway.port,
                                qGateway.protocol,
                                qGateway.gatewayName,
                                qGateway.clientId,
                                qGateway.departmentId,
                                qGateway.description,
                                qGateway.sensorCount,
                                qGateway.thresholdStatus,
                                qGateway.createdAt,
                                qGateway.updatedAt
                        )
                )
                .from(qGateway)
                .where(qGateway.gatewayId.eq(gatewayId))
                .fetchOne();
    }
}
