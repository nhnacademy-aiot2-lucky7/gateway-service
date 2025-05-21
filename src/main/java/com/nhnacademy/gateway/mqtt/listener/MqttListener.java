package com.nhnacademy.gateway.mqtt.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.gateway.gate.dto.GateRequest;
import com.nhnacademy.gateway.gate.service.GateService;
import com.nhnacademy.gateway.mqtt.client.DummyMqttClient;
import com.nhnacademy.gateway.mqtt.dto.TopicInfo;
import com.nhnacademy.gateway.user.common.UserContextHolder;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Component
@Slf4j
public class MqttListener {
    private final MqttClient mqttClient;
    private final DummyMqttClient dummyPublisher;
    private final GateService gateService;
    private final ExecutorService executor;

    private long gateId;
    private boolean collecting = false;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final Set<String> requiredEnvElements =
            Set.of("temperature", "humidity", "dust", "smoke");
    private static final Set<String> requiredDeviceElements =
            Set.of("vibration", "noise", "pdu_voltage", "pdu_current", "pdu_power", "pdu_energy");

    // 위치별로 수신된 env 요소를 기록
    private final Map<String, Set<String>> receivedEnv = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> receivedDevice = new ConcurrentHashMap<>();

    public MqttListener(
            @Qualifier("listenerMqttClient") MqttClient mqttClient,
            DummyMqttClient dummyPublisher,
            GateService gateService
    ) {
        this.mqttClient = mqttClient;
        this.dummyPublisher = dummyPublisher;
        this.gateService = gateService;
        this.executor = Executors.newFixedThreadPool(4);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void subscribe() throws MqttException {
        this.gateId = registerGateway();
        ObjectMapper objectMapper = new ObjectMapper();

        mqttClient.subscribe("data/#", (topic, message) ->
                handleMessage(objectMapper, topic, message));

        log.info("토픽 패턴 구독 완료: data/#");

        collecting = true;
        scheduler.schedule(this::detectMissing, 2, TimeUnit.MINUTES);
        log.info("누락 데이터 검사 스케줄링 완료");
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
                    log.warn("TopicInfo 생성 실패, 메시지 무시됨 - topic: {}", topic);

                    return;
                }

                String newTopic = buildNewTopic(topicInfo, gateId);
                String messageContent = buildNewMessage(timestamp, value);

                publishMessage(newTopic, messageContent);

                trackReceived(topicInfo);

            } catch (Exception e) {
                log.error("메시지 처리 실패 - 토픽: {}, 에러: {}", topic, e.getMessage(), e);
            }
        });
    }

    private void detectMissing() {
        if (!collecting) return;
        collecting = false;
        log.info("=== 누락 데이터 검사 시작 ===");

        // Env 누락 검사
        receivedEnv.forEach((key, set) -> {
            List<String> missing = requiredEnvElements.stream()
                    .filter(e -> !set.contains(e))
                    .toList();
            if (!missing.isEmpty()) {
                String[] parts = key.split("\\|");
                log.info("[Detect] env 누락: {} at {} → {}", parts[0], parts[1], missing);
                dummyPublisher.scheduleDummyElements(parts[0], parts[1], "env", gateId, missing);
            }
        });

        // Device 누락 검사
        receivedDevice.forEach((key, set) -> {
            List<String> missing = requiredDeviceElements.stream()
                    .filter(e -> !set.contains(e))
                    .toList();
            if (!missing.isEmpty()) {
                String[] parts = key.split("\\|");
                log.info("[Detect] device 누락: {} at {} → {}", parts[0], parts[1], missing);
                dummyPublisher.scheduleDummyElements(parts[0], parts[1], "device", gateId, missing);
            }
        });

        log.info("=== 누락 데이터 검사 완료 ===");
        scheduler.shutdown();
    }

    private void trackReceived(TopicInfo info) {
        if (!collecting) return;                         // ❗ 수집 중 아닐 땐 무시
        String key = info.getPlace() + "|" + info.getPosition();
        if ("env".equals(info.getType())) {
            receivedEnv.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                    .add(info.getElement());
        } else {
            receivedDevice.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                    .add(info.getElement());
        }
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

        if (place == null || deviceId == null || position == null || element == null) {
            log.warn("토픽 필수 파트 누락 - topic: {}", topic);

            return null;
        }

        // env 또는 device 판단 로직 : topic 경로 기반 or element 기반 유추
        String type;

        if (topic.contains("/env/")) {
            type = "env";
        } else if (topic.contains("/device/")) {
            type = "device";
        } else {
            // 토픽 경로에 명시가 없다면 e 값으로 유추
            type = inferTypeFromElement(element);
        }

        if (type == null) {
            log.warn("측정 항목에 따른 type 유추 실패 - element: {}", element);

            return null;
        }

        return new TopicInfo(place, type, deviceId, position, element);
    }

    private String inferTypeFromElement(String element) {
        // 측정 항목 이름에 따라 분류
        Set<String> envElements = Set.of("temperature", "humidity", "co2", "noise");
        Set<String> deviceElements = Set.of("power", "current", "voltage", "watt");

        if (envElements.contains(element.toLowerCase())) {
            return "env";
        } else if (deviceElements.contains(element.toLowerCase())) {
            return "device";
        }

        // 타입이 맞지 않으면 null 반환으로 데이터 무시
        return null;
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
                "project-data/s/nhnacademy/b/gyeongnam_campus/p/%s/n/%s/%s/d/%s/g/%d/e/%s",
                info.getPlace(),
                info.getPosition(),
                info.getType(),
                info.getDeviceId(),
                gatewayId,
                info.getElement()
        );
    }

    private String buildNewMessage(long timestamp, double value) {
        return String.format("{\"time\": %d, \"value\": %.2f}", timestamp, value);
    }

    private void publishMessage(String topic, String payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);
        mqttClient.publish(topic, message);
        log.debug("메시지 발행 완료 - 토픽: {}, 페이로드: {}", topic, payload);
    }

    @PreDestroy
    public void shutdownExecutor() {
        executor.shutdown();
        scheduler.shutdown();
        log.info("ExecutorService 정상 종료됨.");
    }
}
