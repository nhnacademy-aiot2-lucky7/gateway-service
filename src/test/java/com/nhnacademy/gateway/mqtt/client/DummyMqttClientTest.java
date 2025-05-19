package com.nhnacademy.gateway.mqtt.client;

import com.nhnacademy.gateway.mqtt.dto.TopicInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class DummyMqttClientTest {

    private DummyMqttClient dummyMqttClient;

    @BeforeEach
    void setUp() {
        dummyMqttClient = new DummyMqttClient(); // 직접 new로 생성 (의존성 없음)
    }

    @Test
    void testBuildTopic() {
        TopicInfo topicInfo = new TopicInfo("office", "env", "dev123", "위치1", "temperature");
        String expected = "dummy_data/s/nhnacademy/b/gyeongnam_campus/p/office/env/d/dev123/n/위치1/e/temperature";

        String actual = dummyMqttClientTestProxy.buildTopic(topicInfo);

        assertEquals(expected, actual);
    }

    @Test
    void testGetPayloadSupplierForTemperature() {
        Supplier<String> supplier = dummyMqttClientTestProxy.getPayloadSupplierForElement("temperature");
        String payload = supplier.get();

        assertTrue(payload.contains("time"));
        assertTrue(payload.contains("value"));
    }

    @Test
    void testGenerateSensorDataRange() {
        for (int i = 0; i < 10; i++) {
            String data = dummyMqttClientTestProxy.generateSensorData(10, 20);
            double value = Double.parseDouble(data.replaceAll("[^0-9.]", "").substring(13));
            assertTrue(value >= 10 && value <= 20, "Value out of range: " + value);
        }
    }

    // 내부 메서드 접근용 서브클래스
    private static class DummyMqttClientTestProxy extends DummyMqttClient {
        public String buildTopic(TopicInfo info) {
            return super.buildTopic(info);
        }

        public Supplier<String> getPayloadSupplierForElement(String element) {
            return super.getPayloadSupplierForElement(element);
        }

        public String generateSensorData(double min, double max) {
            return super.generateSensorData(min, max);
        }
    }

    private final DummyMqttClientTestProxy dummyMqttClientTestProxy = new DummyMqttClientTestProxy();
}
