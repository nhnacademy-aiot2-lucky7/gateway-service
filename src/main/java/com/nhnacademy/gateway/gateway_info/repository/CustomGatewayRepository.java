package com.nhnacademy.gateway.gateway_info.repository;

import com.nhnacademy.gateway.broker.mqtt.dto.MqttBroker;

import java.util.List;

public interface CustomGatewayRepository {

    List<MqttBroker> getMqttBrokers();
}
