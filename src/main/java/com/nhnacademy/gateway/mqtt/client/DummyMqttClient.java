package com.nhnacademy.gateway.mqtt.client;

import com.nhnacademy.gateway.exception.MqttConnectionException;
import com.nhnacademy.gateway.mqtt.dto.TopicInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class DummyMqttClient {

    private final MqttClient client;

    private static final long PUBLISH_INTERVAL_MS = 60000;

    private static final Map<String, List<String>> SPACE_POSITIONS = Map.of(
            "office", List.of("위치1", "위치2", "장비1", "장비2"),
            "class_a", List.of("위치1", "위치2", "장비1", "장비2"),
            "class_b", List.of("위치1", "위치2", "장비1", "장비2"),
            "server_room", List.of("위치1", "위치2", "위치3", "장비1", "장비2"),
            "hive", List.of("위치1", "위치2", "장비1", "장비2"),
            "pair_room", List.of("위치1", "위치2", "장비1", "장비2"),
            "meeting_room", List.of("위치1", "위치2", "장비1", "장비2"),
            "undefined", List.of("위치1")
    );

    private static final List<String> ENV_ELEMENTS = List.of("temperature", "humidity", "dust", "smoke");
    private static final List<String> DEVICE_ELEMENTS = List.of("vibration", "noise", "pdu_voltage", "pdu_current", "pdu_power", "pdu_energy");

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
                        // ENV 센서
                        String deviceId = generateDeviceId();
                        for (String element : ENV_ELEMENTS) {
                            scheduleSensorPublishing(new TopicInfo(place, "env", deviceId, position, element));
                        }
                    } else if (position.startsWith("장비")) {
                        // DEVICE 센서
                        String deviceId = generateDeviceId();
                        for (String element : DEVICE_ELEMENTS) {
                            scheduleSensorPublishing(new TopicInfo(place, "device", deviceId, position, element));
                        }
                    }
                }
            }

        } catch (MqttException e) {
            log.error("MQTT 연결 실패: {}", e.getMessage(), e);

            throw new MqttConnectionException("Mqtt 연결 실패");
        }
    }

    private String generateDeviceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private void scheduleSensorPublishing(TopicInfo topicInfo) {
        Supplier<String> payloadSupplier = getPayloadSupplierForElement(topicInfo.getElement());
        schedulePublishing(topicInfo, payloadSupplier);
    }

    private void schedulePublishing(TopicInfo topicInfo, Supplier<String> payloadSupplier) {
        String topic = buildTopic(topicInfo);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    if (!client.isConnected()) {
                        log.warn("MQTT 클라이언트가 연결되어 있지 않아 메시지를 발행할 수 없습니다.");
                        return;
                    }
                    String payload = payloadSupplier.get();
                    MqttMessage message = new MqttMessage(payload.getBytes());
                    message.setQos(1);
                    client.publish(topic, message);
                    log.debug("메시지 발행 - 토픽: {}, 페이로드: {}", topic, payload);
                } catch (MqttException e) {
                    log.error("메시지 발행 실패 - 토픽: {}, 오류: {}", topic, e.getMessage(), e);

                    throw new MqttConnectionException("Mqtt 메시지 발행 실패");
                }
            }
        }, 0, PUBLISH_INTERVAL_MS);
        log.debug("주기적 발행 설정 완료 - 토픽: {}, 간격: {}ms", topic, PUBLISH_INTERVAL_MS);
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
            case "pdu_voltage" -> this::generateVoltageData;
            case "pdu_current" -> this::generateCurrentData;
            case "pdu_power" -> this::generatePowerData;
            case "pdu_energy" -> this::generateEnergyData;
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
    private String generateCurrentData() { return generateSensorData(0, 20); }
    private String generatePowerData() { return generateSensorData(0, 5000); }
    private String generateEnergyData() { return generateSensorData(0, 100000); }

    String generateSensorData(double min, double max) {
        long time = System.currentTimeMillis();
        Random random = new Random();
        double value = min + random.nextDouble() * (max - min);
        return String.format("{\"time\": %d, \"value\": %.2f}", time, value);
    }
}
