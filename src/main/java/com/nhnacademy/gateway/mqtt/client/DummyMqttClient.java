package com.nhnacademy.gateway.mqtt.client;

import com.nhnacademy.gateway.mqtt.dto.TopicInfo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class DummyMqttClient {

    private static final String BROKER = "tcp://172.19.0.3:1883";
    private MqttClient client;
    private static final long PUBLISH_INTERVAL_MS = 60000;

    private static final Map<String, List<String>> SPACE_POSITIONS = Map.of(
            "office", List.of("position1", "position2", "position3"),
            "class_a", List.of("position1", "position2"),
            "class_b", List.of("position1", "position2"),
            "server_room", List.of("position1", "position2", "position3"),
            "hive", List.of("position1", "position2", "position3"),
            "pair_room", List.of("position1", "position2"),
            "meeting_room", List.of("position1", "position2", "position3"),
            "undefined", List.of("position1")
    );

    private static final List<String> ELEMENTS = List.of("temperature", "humidity", "dust", "smoke");

    @PostConstruct
    public void startPublishing() {
        try {
            client = new MqttClient(BROKER, MqttClient.generateClientId());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.connect(options);
            log.info("MQTT 클라이언트 연결 성공: {}", BROKER);

            // 각 공간에 대해 위치를 순회하면서 데이터를 발행
            for (Map.Entry<String, List<String>> entry : SPACE_POSITIONS.entrySet()) {
                String place = entry.getKey();
                List<String> positions = entry.getValue();
                for (String position : positions) {
                    // 온도와 습도는 동일한 디바이스 ID를 공유
                    String deviceId = generateDeviceIdForTemperatureAndHumidity(place, position);

                    // 온도 센서와 습도 센서는 동일한 deviceId를 사용
                    scheduleSensorPublishing(place, deviceId, position, "temperature");
                    scheduleSensorPublishing(place, deviceId, position, "humidity");

                    // 다른 센서들은 각각 개별적인 deviceId를 사용
                    for (String element : ELEMENTS) {
                        if (!element.equals("temperature") && !element.equals("humidity")) {
                            String individualDeviceId = generateDeviceId();
                            scheduleSensorPublishing(place, individualDeviceId, position, element);
                        }
                    }
                }
            }

        } catch (MqttException e) {
            log.error("MQTT 연결 실패: {}", e.getMessage(), e);
        }
    }

    // 온도와 습도 센서에 대해 동일한 deviceId를 생성
    private String generateDeviceIdForTemperatureAndHumidity(String place, String position) {
        // UUID를 사용하여 고유한 deviceId를 생성
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String generateDeviceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private void scheduleSensorPublishing(String place, String deviceId, String position, String element) {
        TopicInfo topicInfo = new TopicInfo(place, deviceId, position, element);
        Supplier<String> payloadSupplier = getPayloadSupplierForElement(element);
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
                    log.info("메시지 발행 - 토픽: {}, 페이로드: {}", topic, payload);
                } catch (MqttException e) {
                    log.error("메시지 발행 실패 - 토픽: {}, 오류: {}", topic, e.getMessage(), e);
                }
            }
        }, 0, PUBLISH_INTERVAL_MS);
        log.debug("주기적 발행 설정 완료 - 토픽: {}, 간격: {}ms", topic, PUBLISH_INTERVAL_MS);
    }

    private String buildTopic(TopicInfo topicInfo) {

//        return String.format("data/s/nhnacademy/b/gyeongnam_campus/p/%s/d/%s/n/%s/e/%s",
//                topicInfo.getPlace(),
//                topicInfo.getDeviceId(),
//                topicInfo.getPosition(),
//                topicInfo.getElement());

        return String.format("dummy_data/s/nhnacademy/b/gyeongnam_campus/p/%s/d/%s/n/%s/e/%s",
                topicInfo.getPlace(),
                topicInfo.getDeviceId(),
                topicInfo.getPosition(),
                topicInfo.getElement());
    }

    private Supplier<String> getPayloadSupplierForElement(String element) {
        return switch (element) {
            case "temperature" -> this::generateTemperatureData;
            case "humidity" -> this::generateHumidityData;
            case "dust" -> this::generateDustData;
            case "smoke" -> this::generateSmokeData;
            default -> () -> "{}"; // fallback
        };
    }

    private String generateTemperatureData() {
        long time = System.currentTimeMillis();
        Random random = new Random();
        double temperature = 15 + random.nextDouble() * 15; // 온도 범위 15~30도
        return String.format("{\"time\": %d, \"value\": %.2f}", time, temperature);
    }

    private String generateHumidityData() {
        long time = System.currentTimeMillis();
        Random random = new Random();
        double humidity = 30 + random.nextDouble() * 40; // 습도 범위 30~70%
        return String.format("{\"time\": %d, \"value\": %.2f}", time, humidity);
    }

    private String generateDustData() {
        long time = System.currentTimeMillis();
        Random random = new Random();
        double pm10 = 10 + random.nextDouble() * 70;
        double pm25 = 5 + random.nextDouble() * 45;
        double aqi = (pm10 + pm25) / 2;
        return String.format("{\"time\": %d, \"value\": %.1f}", time, aqi);
    }

    private String generateSmokeData() {
        long time = System.currentTimeMillis();
        Random random = new Random();
        double smokeLevel = random.nextDouble() * 5; // 연기 감지 범위 0~5
        return String.format("{\"time\": %d, \"value\": %.2f}", time, smokeLevel);
    }
}