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
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class MqttListener {
    private final MqttClient mqttClient;
    private final DummyMqttClient dummyPublisher;
    private final GateService gateService;
    private final ExecutorService executor;

    private long gateId;

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
                publishMissingDummy(topicInfo);
            } catch (Exception e) {
                log.error("메시지 처리 실패 - 토픽: {}, 에러: {}", topic, e.getMessage(), e);
            }
        });
    }

    private void trackReceived(TopicInfo info) {
        String key = info.getPlace() + "|" + info.getPosition();
        if ("env".equals(info.getType())) {
            receivedEnv.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                    .add(info.getElement());
        } else if ("device".equals(info.getType())) {
            receivedDevice.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                    .add(info.getElement());
        }
    }

    // 누락된 요소가 있으면 즉시 더미 발행
    private void publishMissingDummy(TopicInfo info) {
        String key = info.getPlace() + "|" + info.getPosition(); // deviceId 제거
        Set<String> required = "env".equals(info.getType())
                ? requiredEnvElements
                : requiredDeviceElements;
        Map<String, Set<String>> receivedMap = "env".equals(info.getType())
                ? receivedEnv
                : receivedDevice;
        Set<String> receivedSet = receivedMap.computeIfAbsent(key, k -> new HashSet<>());

        List<String> missing = required.stream()
                .filter(elem -> !receivedSet.contains(elem))
                .toList();
        if (missing.isEmpty()) return;

        if ("env".equals(info.getType())) {
            // 온도/습도는 한 디바이스에 담을 수 있음
            List<String> tempHum = missing.stream()
                    .filter(e -> e.equals("temperature") || e.equals("humidity"))
                    .toList();

            List<String> others = missing.stream()
                    .filter(e -> !tempHum.contains(e))
                    .toList();

            if (!tempHum.isEmpty()) {
                // ❗ 온습도는 하나의 deviceId로
                String deviceId = dummyPublisher.generateDeviceId();
                dummyPublisher.publishDummyElements(
                        info.getPlace(),
                        info.getPosition(),
                        info.getType(),
                        gateId,
                        deviceId,
                        tempHum
                );
                receivedSet.addAll(tempHum);
            }

            for (String elem : others) {
                // ❗ 먼지, 연기 각각에 새로운 deviceId
                String deviceId = dummyPublisher.generateDeviceId();
                dummyPublisher.publishDummyElements(
                        info.getPlace(),
                        info.getPosition(),
                        info.getType(),
                        gateId,
                        deviceId,
                        List.of(elem)
                );
                receivedSet.add(elem);
            }
        } else {
            for (String elem : missing) {
                String deviceId = dummyPublisher.generateDeviceId(); // 각 센서마다
                dummyPublisher.publishDummyElements(
                        info.getPlace(),
                        info.getPosition(),
                        info.getType(),
                        gateId,
                        deviceId,
                        List.of(elem)
                );
                receivedSet.add(elem);
            }
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
        log.info("ExecutorService 정상 종료됨.");
    }
}
