package com.nhnacademy.gateway.modbus;

import com.nhnacademy.gateway.exception.MqttPublishException;
import com.nhnacademy.gateway.gate.dto.GateRequest;
import com.nhnacademy.gateway.gate.service.GateService;
import com.nhnacademy.gateway.modbus.dto.ModbusResult;
import com.nhnacademy.gateway.user.common.UserContextHolder;
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

    private final GateService gateService;

    private static final String BROKER_IP = "115.94.72.197";
    private static final int PORT = 1883;
    private long gateId;

    public ModbusMqttPublisher(@Qualifier("modbusPublisherMqttClient") MqttClient mqttClient, GateService gateService) {
        this.mqttClient = mqttClient;
        this.gateService = gateService;
    }

    public void publish(List<ModbusResult> results) {

        this.gateId = gateService.getGateByAddress(BROKER_IP, PORT).getGateNo();

        for (ModbusResult result : results) {
            publishMetric(result, "voltage", result.getVoltage());
            publishMetric(result, "current", result.getCurrent());
            publishMetric(result, "power", result.getPower());
            publishMetric(result, "energy", result.getEnergy());
        }
    }

    private void publishMetric(ModbusResult result, String metric, double value) {
        try {
            String topic = buildTopic(result, metric);
            String message = buildMessage(result.getTimestamp(), value);

            MqttMessage mqttMessage = new MqttMessage(message.getBytes(StandardCharsets.UTF_8));
            mqttMessage.setQos(1);

            mqttClient.publish(topic, mqttMessage);
            log.debug("토픽 발행 : {}, payload: {}", topic, message);

        } catch (Exception e) {
            String errMsg = String.format("MQTT publish 실패 - metric: %s, location: %s, device: %s",
                    metric, result.getLocation(), result.getDeviceName());
            log.error(errMsg, e);
            throw new MqttPublishException();
        }
    }

    private String buildTopic(ModbusResult result, String metric) {

        String location = result.getLocation();

        String deviceId = deviceIdMap.computeIfAbsent(location, loc -> UUID.randomUUID().toString().replace("-", "").substring(0, 16));

        return String.format(
                "project-data/s/nhnacademy/b/gyeongnam_campus/p/%s/n/%s/device/d/%s/g/%s/e/%s",
                result.getLocation(),
                result.getDeviceName(),
                deviceId,
                gateId,
                metric
        );
    }

    private String buildMessage(long timestamp, double value) {
        return String.format("{\"time\": %d, \"value\": %.2f}", timestamp, value);
    }
}
