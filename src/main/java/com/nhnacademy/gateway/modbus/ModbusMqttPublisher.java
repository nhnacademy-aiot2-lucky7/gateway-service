package com.nhnacademy.gateway.modbus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.gateway.exception.MqttPublishException;
import com.nhnacademy.gateway.modbus.dto.ModbusResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class ModbusMqttPublisher {

    private final MqttClient mqttClient;

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

    private void publishMetric(ModbusResult result, String element, double value) {
        try {
            String topic = String.format("modbus/c/%s/e/%s", result.getChannel(), element);
            String message = buildMessage(result.getTimestamp(), value);

            MqttMessage mqttMessage = new MqttMessage(message.getBytes(StandardCharsets.UTF_8));
            mqttMessage.setQos(1);

            mqttClient.publish(topic, mqttMessage);
            log.info("Published to topic: {}, payload: {}", topic, message);

        } catch (Exception e) {
            String errMsg = String.format("MQTT publish 실패 - topic: modbus/c/%s/e/%s", result.getChannel(), element);
            log.error(errMsg, e);
            throw new MqttPublishException();
        }
    }

    private String buildMessage(long timestamp, double value) {
        return String.format("{\"time\": %d, \"value\": %.2f}", timestamp, value);
    }
}
