package com.nhnacademy.gateway.mqtt.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.gateway.gate.dto.GateRequest;
import com.nhnacademy.gateway.gate.service.GateService;
import com.nhnacademy.gateway.mqtt.dto.TopicInfo;
import com.nhnacademy.gateway.user.common.UserContextHolder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MqttListener {
    private final MqttClient mqttClient;
    private final GateService gateService;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private long gateId;

    @PostConstruct
    public void subscribe() throws MqttException {
        this.gateId = registerGateway();
        ObjectMapper objectMapper = new ObjectMapper();

        mqttClient.subscribe("dummy_data/#", (topic, message) ->
                handleMessage(objectMapper, topic, message));

        log.info("Subscribed to topic pattern: dummy_data/#");
    }

    private void handleMessage(ObjectMapper objectMapper, String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        log.debug("Received message - topic: {}, payload: {}", topic, payload);

        executor.submit(() -> {
            try {
                JsonNode node = objectMapper.readTree(payload);
                double value = node.get("value").asDouble();
                long timestamp = node.has("time") ? node.get("time").asLong() : System.currentTimeMillis();

                TopicInfo topicInfo = parseTopicParts(topic);
                if (topicInfo == null) {
                    log.warn("Invalid topic format: {}", topic);
                    return;
                }

                String newTopic = buildNewTopic(topicInfo, gateId);
                String messageContent = buildNewMessage(timestamp, value);

                publishMessage(newTopic, messageContent);
            } catch (Exception e) {
                log.error("Failed to process message - topic: {}, error: {}", topic, e.getMessage(), e);
            }
        });
    }

    private long registerGateway() {
        UserContextHolder.setDepartmentId("master");

        GateRequest gateRegisterRequest = new GateRequest(
                "기존 데이터",
                "MQTT",
                "115.94.72.197",
                1883,
                "nhnacademy 서버의 센서 수집 데이터"
        );

        long id = gateService.createGate(gateRegisterRequest);
        log.info("Registered gateway with ID: {}", id);
        return id;
    }

    private TopicInfo parseTopicParts(String topic) {
        String[] parts = topic.split("/");

        String place = getPart(parts, "p");
        String deviceId = getPart(parts, "d");
        String position = getPart(parts, "n");
        String element = getPart(parts, "e");

        if (place == null || deviceId == null || position == null || element == null) {
            return null;
        }

        return new TopicInfo(place, deviceId, position, element);
    }

    private String getPart(String[] parts, String key) {
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].equals(key)) {
                return parts[i + 1];
            }
        }
        return null;
    }

    private String buildNewTopic(TopicInfo info, long gatewayId) {
        return String.format(
                "project-data/s/nhnacademy/b/gyeongnam_campus/p/%s/d/%s/g/%d/n/%s/e/%s",
                info.getPlace(), info.getDeviceId(), gatewayId, info.getPosition(), info.getElement()
        );
    }

    private String buildNewMessage(long timestamp, double value) {
        return String.format("{\"time\": %d, \"value\": %.2f}", timestamp, value);
    }

    private void publishMessage(String topic, String payload) throws MqttException {
        MqttMessage message = new MqttMessage();
        message.setPayload(payload.getBytes());
        message.setQos(1); // QOS 설정 필요시 조정

        mqttClient.publish(topic, message);
        log.debug("Published message - topic: {}, payload: {}", topic, payload);
    }

    @PreDestroy
    public void shutdownExecutor() {
        executor.shutdown();
        log.info("ExecutorService shut down successfully.");
    }
}
