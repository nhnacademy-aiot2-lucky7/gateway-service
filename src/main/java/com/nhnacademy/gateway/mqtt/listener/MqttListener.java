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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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

    @EventListener(ApplicationReadyEvent.class)
    public void subscribe() throws MqttException {
        this.gateId = registerGateway();
        ObjectMapper objectMapper = new ObjectMapper();

        mqttClient.subscribe("dummy_data/#", (topic, message) ->
                handleMessage(objectMapper, topic, message));

        log.info("토픽 패턴 구독 완료: dummy_data/#");
    }

    private void handleMessage(ObjectMapper objectMapper, String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        log.debug("메시지 수신 - 토픽: {}, 페이로드: {}", topic, payload);

        executor.submit(() -> {
            try {
                JsonNode node = objectMapper.readTree(payload);
                double value = node.get("value").asDouble();
                long timestamp = node.has("time") ? node.get("time").asLong() : System.currentTimeMillis();

                TopicInfo topicInfo = parseTopicParts(topic);
                if (topicInfo == null) {
                    log.warn("잘못된 토픽 형식: {}", topic);
                    return;
                }

                String newTopic = buildNewTopic(topicInfo, gateId);
                String messageContent = buildNewMessage(timestamp, value);

                publishMessage(newTopic, messageContent);
            } catch (Exception e) {
                log.error("메시지 처리 실패 - 토픽: {}, 에러: {}", topic, e.getMessage(), e);
            }
        });
    }

    private long registerGateway() {
        UserContextHolder.setDepartmentId("master");

        GateRequest gateRegisterRequest = new GateRequest(
                "기존 데이터", "MQTT", "115.94.72.197", 1883,
                "nhnacademy 서버의 센서 수집 데이터"
        );

        long id = gateService.createGate(gateRegisterRequest);
        log.info("게이트웨이 등록 완료 - ID: {}", id);
        return id;
    }

    private TopicInfo parseTopicParts(String topic) {
        String[] parts = topic.split("/");

        String place = getPart(parts, "p");
        String deviceId = getPart(parts, "d");
        String position = getPart(parts, "n");
        String element = getPart(parts, "e");

        // env, device 여부 판단
        String type = topic.contains("/env/") ? "env" :
                topic.contains("/device/") ? "device" : null;

        if (place == null || deviceId == null || position == null || element == null || type == null) {
            return null;
        }

        return new TopicInfo(place, type, deviceId, position, element);
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
                "project-data/s/nhnacademy/b/gyeongnam_campus/p/%s/%s/d/%s/g/%d/n/%s/e/%s",
                info.getPlace(),
                info.getType(),
                info.getDeviceId(),
                gatewayId,
                info.getPosition(),
                info.getElement()
        );
    }

    private String buildNewMessage(long timestamp, double value) {
        return String.format("{\"time\": %d, \"value\": %.2f}", timestamp, value);
    }

    private void publishMessage(String topic, String payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1); // QOS 설정 필요 시 변경
        mqttClient.publish(topic, message);
        log.debug("메시지 발행 완료 - 토픽: {}, 페이로드: {}", topic, payload);
    }

    @PreDestroy
    public void shutdownExecutor() {
        executor.shutdown();
        log.info("ExecutorService 정상 종료됨.");
    }
}
