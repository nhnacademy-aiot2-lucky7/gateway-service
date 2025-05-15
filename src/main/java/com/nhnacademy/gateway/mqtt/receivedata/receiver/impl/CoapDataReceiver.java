package com.nhnacademy.gateway.mqtt.receivedata.receiver.impl;

import com.nhnacademy.gateway.exception.CommonHttpException;
import com.nhnacademy.gateway.mqtt.receivedata.dto.DataRequest;
import com.nhnacademy.gateway.mqtt.receivedata.receiver.GatewayReceiver;
import com.nhnacademy.gateway.mqtt.client.GatewayConnector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoapDataReceiver implements GatewayReceiver {

    @Lazy
    @Autowired
    private GatewayConnector gatewayConnector;

    private CoapServer coapServer;
    private final Set<String> registeredGateways = ConcurrentHashMap.newKeySet();

    @Override
    public synchronized void start(String gateBrokerUrl, String gatewayId, String topic) {
        if (coapServer == null) {
            coapServer = new CoapServer(5683);
            coapServer.start();
            log.info("[CoAP 수신기] CoAP 서버 시작됨");
        }

        if (registeredGateways.add(gatewayId)) {
            coapServer.add(new DataResource(gatewayId, topic));
            log.info("[CoAP 수신기] 리소스 등록 완료 - gatewayId: {}", gatewayId);
        } else {
            log.info("[CoAP 수신기] 이미 등록된 gatewayId - {}", gatewayId);
        }
    }

    @Getter
    public class DataResource extends CoapResource {
        private final String gatewayId;
        private final String topic;

        public DataResource(String gatewayId, String topic) {
            super(gatewayId);
            this.gatewayId = gatewayId;
            this.topic = topic;
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            String payload = exchange.getRequestText();
            log.info("[CoAP] 데이터 수신 - gatewayId: {}, payload: {}", gatewayId, payload);

            try {
                Map<String, String> map = Arrays.stream(payload.split(","))
                        .map(s -> s.split("=", 2))
                        .filter(arr -> arr.length == 2)
                        .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));

                if (!map.containsKey("time") || !map.containsKey("value")) {
                    exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Invalid payload");
                    return;
                }

                long time = Long.parseLong(map.get("time"));
                double value = Double.parseDouble(map.get("value"));

                DataRequest request = new DataRequest(topic, time, value);
                gatewayConnector.receiveGatewayData(topic, request);

                exchange.respond(CoAP.ResponseCode.CONTENT, "OK");
            } catch (Exception e) {
                log.error("[CoAP] 데이터 처리 실패", e);
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Invalid payload");
            }
        }
    }
}
