package com.nhnacademy.gateway.broker.mqtt;

import com.nhnacademy.gateway.broker.mqtt.dto.BrokerType;
import com.nhnacademy.gateway.broker.mqtt.dto.MqttBroker;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MqttClientFactory {

    private final Map<BrokerType, MqttCallbackExtended> callbackMap;

    public MqttClientFactory(List<MqttCallbackStrategy> strategies) {
        this.callbackMap = strategies.stream()
                .collect(
                        Collectors.toMap(
                                MqttCallbackStrategy::getBrokerType,
                                MqttCallbackStrategy::getMqttCallbackImpl
                        )
                );
    }

    public IMqttAsyncClient createMqttClient(MqttBroker mqttBroker) throws MqttException {
        IMqttAsyncClient client = new MqttAsyncClient(
                mqttBroker.getServerURI(),
                mqttBroker.getBuildClientIdWithTimestamp(),
                new MemoryPersistence()
        );
        client.setCallback(
                callbackMap.get(mqttBroker.getBrokerType())
        );
        return client;
    }

    public List<IMqttAsyncClient> createInboundBrokerClients(List<MqttBroker> mqttBrokers) {
        List<IMqttAsyncClient> inboundClients = new ArrayList<>();
        for (MqttBroker mqttBroker : mqttBrokers) {
            try {
                inboundClients.add(createMqttClient(mqttBroker));
            } catch (MqttException e) {
                log.warn("[Client ID: {} / serverURI: {}]의 데이터 형식이 잘못되어 있습니다 - {}",
                        mqttBroker.getClientId(), mqttBroker.getServerURI(), e.getMessage(), e);
            }
        }
        return inboundClients;
    }
}
