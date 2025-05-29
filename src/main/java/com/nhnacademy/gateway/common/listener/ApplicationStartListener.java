package com.nhnacademy.gateway.common.listener;

import com.nhnacademy.gateway.broker.mqtt.MqttClientFactory;
import com.nhnacademy.gateway.broker.mqtt.dto.MqttBroker;
import com.nhnacademy.gateway.common.parser.DataParser;
import com.nhnacademy.gateway.gateway_info.repository.GatewayRepository;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

/// TODO: 임시
@Slf4j
@Component
public class ApplicationStartListener implements ApplicationListener<ApplicationReadyEvent> {

    private final GatewayRepository gatewayRepository;

    private final MqttClientFactory mqttClientFactory;

    private final MqttBroker mqttCoreBroker;

    private final DataParser dataParser;

    public ApplicationStartListener(
            GatewayRepository gatewayRepository,
            MqttClientFactory mqttClientFactory,
            @Qualifier("mqttCoreBroker") MqttBroker mqttCoreBroker,
            @Qualifier("jsonDataParser") DataParser dataParser) {
        this.gatewayRepository = gatewayRepository;
        this.mqttClientFactory = mqttClientFactory;
        this.mqttCoreBroker = mqttCoreBroker;
        this.dataParser = dataParser;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        IMqttAsyncClient coreClient = coreClient();
        try {
            coreClient.connect().waitForCompletion();
        } catch (MqttException e) {
            log.error("clientId: {}", coreClient.getClientId());
            log.error("serverURI: {}", coreClient.getServerURI());
            throw new RuntimeException("Core Broker 구독 실패!: " + e.getMessage());
        }

        List<MqttBroker> mqttBrokers = gatewayRepository.getMqttBrokers();
        List<IMqttAsyncClient> inboundClients = inboundClient(mqttBrokers);
        if (mqttBrokers.size() != inboundClients.size()) {
            throw new RuntimeException("Inbound Broker 구독 실패!");
        }

        for (int n = 0; n < mqttBrokers.size(); n++) {
            IMqttAsyncClient inboundClient = inboundClients.get(n);

            inboundClient
                    .setCallback(
                            new ForwardingMqttCallback(
                                    mqttBrokers.get(n).getGatewayId(),
                                    coreClient,
                                    dataParser
                            )
                    );

            try {
                inboundClient.connect().waitForCompletion();
                inboundClient.subscribe("data/#", 1);
            } catch (MqttException e) {
                throw new RuntimeException("Inbound Broker 구독 실패!: " + e.getMessage());
            }
        }
    }

    private IMqttAsyncClient coreClient() {
        try {
            return mqttClientFactory.createMqttClient(mqttCoreBroker);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private List<IMqttAsyncClient> inboundClient(List<MqttBroker> mqttBrokers) {
        return mqttClientFactory.createInboundBrokerClients(mqttBrokers);
    }
}
