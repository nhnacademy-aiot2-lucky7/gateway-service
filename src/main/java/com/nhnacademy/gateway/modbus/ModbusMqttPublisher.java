package com.nhnacademy.gateway.modbus;

import com.nhnacademy.gateway.exception.MqttPublishException;
import com.nhnacademy.gateway.modbus.dto.ModbusResult;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ModbusMqttPublisher {

    private final MqttClient mqttClient;

    private final Map<String, String> deviceIdMap = new ConcurrentHashMap<>();

    public ModbusMqttPublisher(@Qualifier("modbusPublisherMqttClient") MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public void publish(List<ModbusResult> results) {
        for (ModbusResult result : results) {
            publishMetric(result, "voltage", result.getVoltage());
            publishMetric(result, "current", result.getCurrent());
            publishMetric(result, "power", result.getPower());
        }
    }

    private void publishMetric(ModbusResult result, String metric, double value) {
        try {
            String topic = buildTopic(result, metric);
            String message = buildMessage(result.getTimestamp(), value);

            MqttMessage mqttMessage = new MqttMessage(message.getBytes(StandardCharsets.UTF_8));
            mqttMessage.setQos(1);

            mqttClient.publish(topic, mqttMessage);
            log.info("Published to topic: {}, payload: {}", topic, message);

        } catch (Exception e) {
            String errMsg = String.format("MQTT publish 실패 - metric: %s, location: %s, device: %s",
                    metric, result.getLocation(), result.getDeviceName());
            log.error(errMsg, e);
            throw new MqttPublishException();
        }
    }

    private String buildTopic(ModbusResult result, String metric) {
        String key = result.getLocation() + ":" + result.getDeviceName();
        String deviceId = deviceIdMap.computeIfAbsent(key, k -> UUID.randomUUID().toString());

        return String.format(
                "project-data/s/nhnacademy/b/gyeongnam_campus/p/%s/n/%s/device/d/%s/g/34/e/%s",
                result.getLocation(),
                result.getDeviceName(),
                deviceId,
                metric
        );
    }

    private String buildMessage(long timestamp, double value) {
        return String.format("{\"time\": %d, \"value\": %.2f}", timestamp, value);
    }
}
