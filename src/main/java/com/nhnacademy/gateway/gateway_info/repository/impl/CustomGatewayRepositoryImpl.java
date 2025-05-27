package com.nhnacademy.gateway.gateway_info.repository.impl;

import com.nhnacademy.gateway.broker.mqtt.dto.MqttBroker;
import com.nhnacademy.gateway.broker.mqtt.dto.QMqttBroker;
import com.nhnacademy.gateway.common.enums.IoTProtocol;
import com.nhnacademy.gateway.gateway_info.domain.Gateway;
import com.nhnacademy.gateway.gateway_info.domain.QGateway;
import com.nhnacademy.gateway.gateway_info.repository.CustomGatewayRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

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
    public List<MqttBroker> getMqttBrokers() {
        return queryFactory
                .select(
                        new QMqttBroker(
                                qGateway.ipAddress,
                                qGateway.port,
                                qGateway.clientId
                        )
                )
                .from(qGateway)
                .where(qGateway.protocol.eq(IoTProtocol.MQTT))
                .fetch();
    }
}
