package com.nhnacademy.gateway.mqtt.client;

import com.nhnacademy.gateway.exception.MqttConnectionException;
import com.nhnacademy.gateway.mqtt.dto.TopicInfo;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

@Slf4j
@Component
public class DummyMqttClient {

    private final MqttClient client;
    private static final Random RANDOM = new Random(); // 수정: 랜덤값 생성용

    private final List<PublishTask> publishTasks = new CopyOnWriteArrayList<>();

    private static final long PUBLISH_INTERVAL_MS = 60000;

    private static final Map<String, List<String>> SPACE_POSITIONS = Map.of(
            "office", List.of("위치1", "위치2", "장비1", "장비2"),
            "class_a", List.of("위치1", "위치2", "장비1", "장비2"),
            "class_b", List.of("위치1", "위치2", "장비1", "장비2"),
            "server_room", List.of("위치1", "위치2", "위치3", "장비1", "장비2", "장비3", "장비4", "장비5", "장비6", "장비7", "장비8", "장비9", "장비10"),
            "hive", List.of("위치1", "위치2", "장비1", "장비2"),
            "pair_room", List.of("위치1", "위치2", "장비1", "장비2"),
            "meeting_room", List.of("위치1", "위치2", "장비1", "장비2"),
            "undefined", List.of("위치1")
    );

    public DummyMqttClient(@Qualifier("dummyPublisherMqttClient") MqttClient client) {
        this.client = client;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startPublishing() {
        try {
            if (!client.isConnected()) {
                client.reconnect();
            }
            log.info("Dummy MQTT 클라이언트 실행, 브로커: {}", client.getServerURI());

            for (Map.Entry<String, List<String>> entry : SPACE_POSITIONS.entrySet()) {
                String place = entry.getKey();
                List<String> positions = entry.getValue();

                for (String position : positions) {
                    if (position.startsWith("위치")) {
                        // 온습도는 하나의 센서로 측정
                        String envDeviceId1 = generateDeviceId();
                        publishTasks.add(new PublishTask(buildTopic(new TopicInfo(place, "env", envDeviceId1, position, "temperature")), getPayloadSupplierForElement("temperature")));
                        publishTasks.add(new PublishTask(buildTopic(new TopicInfo(place, "env", envDeviceId1, position, "humidity")), getPayloadSupplierForElement("humidity")));

                        // 먼지 센서
                        String envDeviceId2 = generateDeviceId();
                        publishTasks.add(new PublishTask(buildTopic(new TopicInfo(place, "env", envDeviceId2, position, "dust")), getPayloadSupplierForElement("dust")));

                        // 연기 센서
                        String envDeviceId3 = generateDeviceId();
                        publishTasks.add(new PublishTask(buildTopic(new TopicInfo(place, "env", envDeviceId3, position, "smoke")), getPayloadSupplierForElement("smoke")));

                    } else if (position.startsWith("장비")) {
                        // 진동 센서
                        String vibrationDeviceId = generateDeviceId();
                        publishTasks.add(new PublishTask(buildTopic(new TopicInfo(place, "device", vibrationDeviceId, position, "vibration")), getPayloadSupplierForElement("vibration")));

                        // 소음 센서
                        String noiseDeviceId = generateDeviceId();
                        publishTasks.add(new PublishTask(buildTopic(new TopicInfo(place, "device", noiseDeviceId, position, "noise")), getPayloadSupplierForElement("noise")));

                        // PDU 센서 (server_room에 한정)
                        if ("server_room".equals(place)) {
                            String pduDeviceId = generateDeviceId();
                            for (String element : List.of("voltage", "current", "power", "energy")) {
                                TopicInfo info = new TopicInfo(place, "device", pduDeviceId, position, element);
                                publishTasks.add(new PublishTask(buildTopic(info), getPayloadSupplierForElement(element)));
                            }
                        }
                    }
                }
            }

        } catch (MqttException e) {
            log.error("MQTT 연결 실패: {}", e.getMessage(), e);
            throw new MqttConnectionException("Mqtt 연결 실패");
        }
    }

    // 일정 간격으로 모든 토픽에 메시지 발행
    @Scheduled(fixedRate = PUBLISH_INTERVAL_MS)
    public void scheduledPublishing() {
        for (PublishTask task : publishTasks) {
            try {
                if (!client.isConnected()) {
                    log.warn("MQTT 클라이언트가 연결되어 있지 않아 메시지를 발행할 수 없습니다.");
                    continue;
                }
                String payload = task.payloadSupplier.get();
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(0);
                client.publish(task.topic, message);
                log.debug("메시지 발행 - 토픽: {}, 페이로드: {}", task.topic, payload);
            } catch (MqttException e) {
                log.error("메시지 발행 실패 - 토픽: {}, 오류: {}", task.topic, e.getMessage(), e);
                throw new MqttConnectionException("Mqtt 메시지 발행 실패");
            }
        }
    }

    public void scheduleDummyElements(
            String place,
            String position,
            String type,
            String deviceId,
            long gatewayId,
            List<String> elements
    ) {
        log.info("[DummyScheduler] 누락된 {} [{}] 을(를) 디바이스ID={} 로 스케줄링합니다.", type, elements, deviceId);

        for (String elem : elements) {
            String topic = String.format(
                    "project-data/s/nhnacademy/b/gyeongnam_campus/p/%s/n/%s/%s/d/%s/g/%d/e/%s",
                    place, position, type, deviceId, gatewayId, elem
            );
            publishTasks.add(new PublishTask(topic, getPayloadSupplierForElement(elem)));
        }
    }

    public String generateDeviceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    String buildTopic(TopicInfo topicInfo) {
        return String.format("data/s/nhnacademy/b/gyeongnam_campus/p/%s/%s/d/%s/n/%s/e/%s",
                topicInfo.getPlace(),
                topicInfo.getType(),
                topicInfo.getDeviceId(),
                topicInfo.getPosition(),
                topicInfo.getElement());
    }

    Supplier<String> getPayloadSupplierForElement(String element) {
        return switch (element) {
            case "temperature" -> this::generateTemperatureData;
            case "humidity" -> this::generateHumidityData;
            case "dust" -> this::generateDustData;
            case "smoke" -> this::generateSmokeData;
            case "vibration" -> this::generateVibrationData;
            case "noise" -> this::generateNoiseData;
            case "voltage" -> this::generateVoltageData;
            case "current" -> this::generateCurrentData;
            case "power" -> this::generatePowerData;
            case "energy" -> this::generateEnergyData;
            default -> () -> "{}";
        };
    }

    // 센서 값 생성 메서드들
    private String generateTemperatureData() { return generateSensorData(15, 30); }
    private String generateHumidityData() { return generateSensorData(30, 70); }
    private String generateDustData() { return generateSensorData(10, 80); }
    private String generateSmokeData() { return generateSensorData(0, 5); }
    private String generateVibrationData() { return generateSensorData(0, 10); }
    private String generateNoiseData() { return generateSensorData(30, 90); }
    private String generateVoltageData() { return generateSensorData(210, 240); }
    private String generateCurrentData() { return generateSensorData(0, 10); }  // 최대 10A
    private String generatePowerData() { return generateSensorData(0, 2000); }   // 최대 2kW

    private long energyCounter = 0;
    private String generateEnergyData() {
        // 1분마다 0~100 Wh 정도 누적 증가 가정
        energyCounter += RANDOM.nextInt(100);
        long time = System.currentTimeMillis();
        return String.format("{\"time\": %d, \"value\": %d}", time, energyCounter);
    }

    String generateSensorData(double min, double max) {
        long time = System.currentTimeMillis();
        Random random = new Random();
        double value = min + random.nextDouble() * (max - min);
        return String.format("{\"time\": %d, \"value\": %.2f}", time, value);
    }

    // 내부 클래스: 토픽과 페이로드 생성기를 보관
    private static class PublishTask {
        String topic;
        Supplier<String> payloadSupplier;

        PublishTask(String topic, Supplier<String> payloadSupplier) {
            this.topic = topic;
            this.payloadSupplier = payloadSupplier;
        }
    }
}
