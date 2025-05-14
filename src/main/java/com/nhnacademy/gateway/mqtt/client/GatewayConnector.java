package com.nhnacademy.gateway.mqtt.client;

import com.nhnacademy.gateway.mqtt.receivedata.dto.DataRequest;
import com.nhnacademy.gateway.mqtt.receivedata.receiver.impl.CoapDataReceiver;
import com.nhnacademy.gateway.mqtt.receivedata.receiver.impl.HttpDataReceiver;
import com.nhnacademy.gateway.mqtt.receivedata.receiver.impl.MqttDataReceiver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayConnector {

    // 허브 브로커 URL (고정 혹은 설정에서 주입해도 됨)
    private static final String HUB_BROKER = "tcp://172.19.0.2:1883"; // 필요 시 수정
    private static final String HUB_CLIENT_ID = "hub-client";

    @Lazy
    private final MqttDataReceiver mqttReceiver;

    @Lazy
    private final HttpDataReceiver httpReceiver;

    @Lazy
    private final CoapDataReceiver coapReceiver;

    // 게이트웨이별 데이터 전송 스케줄러
    private final Map<String, ScheduledExecutorService> schedulerMap = new ConcurrentHashMap<>();

    // 게이트웨이에서 수신한 데이터 큐
    private final Queue<DataRequest> gatewayDataQueue = new ConcurrentLinkedQueue<>();

    // MQTT 클라이언트 캐시
    private final Map<String, MqttClient> clientMap = new ConcurrentHashMap<>();

    /**
     * 게이트웨이 시작
     */
    public void startGateway(String brokerUrl, String gatewayId, String protocol) {
        log.info("게이트웨이 연결 시작 - gatewayId: {}, protocol: {}", gatewayId, protocol);
        String topic = gatewayId + "/data";

        switch (protocol.toUpperCase()) {
            case "MQTT" -> mqttReceiver.start(brokerUrl, gatewayId, topic);
            case "HTTP" -> httpReceiver.start(brokerUrl, gatewayId, topic);
            case "COAP" -> coapReceiver.start(brokerUrl, gatewayId, topic);
            default -> {
                log.warn("지원하지 않는 프로토콜: {}", protocol);
                return;
            }
        }

        // 허브로 전송하는 작업을 2초마다 실행
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> publishToHub(gatewayId), 0, 2, TimeUnit.SECONDS);
        schedulerMap.put(gatewayId, scheduler);
    }

    /**
     * 게이트웨이에서 수신한 데이터 저장
     */
    public void receiveGatewayData(String topic, DataRequest dataRequest) {
        gatewayDataQueue.offer(dataRequest);
    }

    /**
     * 허브 브로커로 데이터 재발행
     */
    private void publishToHub(String gatewayId) {
        if (gatewayDataQueue.isEmpty()) {
            return;
        }

        try {
            // 허브 MQTT 클라이언트 준비 (싱글턴)
            MqttClient hubClient = clientMap.computeIfAbsent("hub", key -> {
                try {
                    MqttClient client = new MqttClient(HUB_BROKER, HUB_CLIENT_ID);
                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setCleanSession(true);
                    client.connect(options);
                    return client;
                } catch (MqttException e) {
                    log.error("허브 브로커 연결 실패: {}", e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            });

            StringBuilder batchPayload = new StringBuilder();

            while (!gatewayDataQueue.isEmpty()) {
                DataRequest dr = gatewayDataQueue.poll();
                batchPayload.append(dr.getTopic())
                        .append(" time=").append(dr.getTime())
                        .append(" value=").append(dr.getValue())
                        .append("\n");
            }

            if (batchPayload.length() > 0) {
                MqttMessage message = new MqttMessage(batchPayload.toString().getBytes());
                message.setQos(1);
                hubClient.publish("project-data/" + gatewayId, message);
                log.debug("허브로 재발행 완료 - payload:\n{}", batchPayload);
            }

        } catch (MqttException e) {
            log.error("허브 재발행 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 게이트웨이 종료
     */
    public void shutdown(String gatewayId) {
        ScheduledExecutorService scheduler = schedulerMap.remove(gatewayId);
        if (scheduler != null) {
            scheduler.shutdownNow();
        }

        MqttClient client = clientMap.remove(gatewayId);
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
                client.close();
            } catch (MqttException e) {
                log.warn("게이트웨이 MQTT 클라이언트 종료 실패: {}", e.getMessage());
            }
        }

        log.info("게이트웨이 연결 종료 - {}", gatewayId);
    }
}
