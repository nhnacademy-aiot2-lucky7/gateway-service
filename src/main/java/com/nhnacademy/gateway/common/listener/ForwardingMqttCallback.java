package com.nhnacademy.gateway.common.listener;

import com.nhnacademy.gateway.common.parser.DataParser;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/// TODO: 임시
@Slf4j
public final class ForwardingMqttCallback implements MqttCallback {

    private final long gatewayNo;

    private final IMqttAsyncClient targetClient;

    private final DataParser dataParser;

    public ForwardingMqttCallback(
            long gatewayNo, IMqttAsyncClient targetClient,
            DataParser dataParser
    ) {
        this.gatewayNo = gatewayNo;
        this.targetClient = targetClient;
        this.dataParser = dataParser;
    }

    @Override
    public void connectionLost(Throwable cause) {
        // 로그 기록 혹은 재연결 시도 로직
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        log.debug("topic: {}", topic);
        log.debug("message: {}", message.toString());
        String[] data = topic.split("/");

        // 공간
        String p = "null";

        // 센서 아이디
        String d = "null";

        // 상세 위치
        String n = "null";

        // 데이터 타입
        String e = "null";

        for (int i = 0; i < data.length; i++) {
            switch (data[i]) {
                case "p":
                    p = data[++i];
                    break;
                case "d":
                    d = data[++i];
                    break;
                case "n":
                    n = data[++i];
                    break;
                case "e":
                    e = data[++i];
                    break;
                default:
            }
        }

        // 순서(공간 -> 상세 위치 -> 게이트웨이 아이디 -> 센서 아이디 -> 데이터 타입)
        String newTopic = "team1_data/s/nhnacademy/b/gyeongnam_campusp/p/%s/n/%s/g/%d/d/%s/e/%s"
                .formatted(p, n, gatewayNo, d, e);

        log.debug("newTopic: {}", newTopic);
        log.debug("==================================");

        try {
            payloadParsing(new String(message.getPayload()));
        } catch (Exception ex) {
            return;
        }

        try {
            targetClient.publish(newTopic, message);
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // 필요 시 로깅
    }

    private void payloadParsing(String payload) throws IOException {
        Map<String, Object> parsing = dataParser.parsing(payload);

        Object rawValue = parsing.get("value");
        double value = 0.0;
        if (rawValue instanceof Number number) {
            value = number.doubleValue();
        } else if (rawValue instanceof String string) {
            value = Double.parseDouble(string);
        }

        Object rawTime = parsing.get("time");
        long timestamp = 0L;
        if (rawTime instanceof Number number) {
            timestamp = number.longValue();
        } else if (rawTime instanceof String string) {
            try {
                timestamp = Long.parseLong(string);
            } catch (NumberFormatException ignored) {
                timestamp = Instant.now().getEpochSecond();
            }
        }
        log.debug("value: {}", value);
        log.debug("timestamp: {}", timestamp);
    }
}
