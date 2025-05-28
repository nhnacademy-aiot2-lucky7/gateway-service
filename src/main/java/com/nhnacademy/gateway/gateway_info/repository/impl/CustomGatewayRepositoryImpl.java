package com.nhnacademy.gateway.gateway_info.repository.impl;

import com.nhnacademy.gateway.broker.mqtt.dto.MqttBroker;
import com.nhnacademy.gateway.broker.mqtt.dto.QMqttInboundBroker;
import com.nhnacademy.gateway.common.enums.IoTProtocol;
import com.nhnacademy.gateway.gateway_info.domain.Gateway;
import com.nhnacademy.gateway.gateway_info.domain.QGateway;
import com.nhnacademy.gateway.gateway_info.repository.CustomGatewayRepository;
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
    public String getDepartmentIdByGatewayNo(long gatewayNo) {
        return queryFactory
                .select(qGateway.departmentId)
                .from(qGateway)
                .where(qGateway.gatewayNo.eq(gatewayNo))
                .fetchOne();
    }

    @Override
    public List<MqttBroker> getMqttBrokers() {
        return new ArrayList<>(
                queryFactory
                        .select(
                                new QMqttInboundBroker(
                                        qGateway.gatewayNo,
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
}
