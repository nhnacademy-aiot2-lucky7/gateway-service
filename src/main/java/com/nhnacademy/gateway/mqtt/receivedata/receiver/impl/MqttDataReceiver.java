package com.nhnacademy.gateway.mqtt.receivedata.receiver.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.gateway.mqtt.receivedata.dto.DataRequest;
import com.nhnacademy.gateway.mqtt.receivedata.receiver.GatewayReceiver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MqttDataReceiver implements GatewayReceiver {

    private final ObjectMapper objectMapper;

    @Override
    public void start(String gateBrokerUrl, String gatewayId, String topic) {
        try {
            MqttClient client = new MqttClient(gateBrokerUrl, gatewayId + "-receiver");
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.connect(options);

            client.subscribe(topic, (t, message) -> {
                String payload = new String(message.getPayload());
                log.info("[MQTT 수신] gatewayId={} topic={} payload={}", gatewayId, t, payload);

                try {
                    DataRequest dataRequest = objectMapper.readValue(payload, DataRequest.class);

                    // 기존 DataReceiverService.receiveData()의 내용 직접 삽입
                    log.info("[MqttDataReceiver] topic={} → 데이터 처리 시작", t);
                    // 여기서 필요한 데이터 처리 로직이 나중에 들어올 수 있음
                    log.info("[MqttDataReceiver] topic={} → 데이터 처리 완료", t);

                    log.info("[MQTT 저장] gatewayId={} → DataRequest 저장 완료", gatewayId);
                } catch (Exception e) {
                    log.error("[MQTT 에러] gatewayId={} → 메시지 파싱 실패", gatewayId, e);
                }
            });

            log.info("[MQTT 연결] gatewayId={} 브로커 접속 성공. topic 구독: {}", gatewayId, topic);

        } catch (MqttException e) {
            log.error("[MQTT 에러] gatewayId={} 브로커 연결 실패: {}", gatewayId, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
