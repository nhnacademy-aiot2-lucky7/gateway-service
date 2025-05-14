package com.nhnacademy.gateway.mqtt.receivedata.receiver.impl;

import com.nhnacademy.gateway.mqtt.client.GatewayConnector;
import com.nhnacademy.gateway.mqtt.receivedata.dto.DataRequest;
import com.nhnacademy.gateway.mqtt.receivedata.receiver.GatewayReceiver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoapDataReceiver implements GatewayReceiver {

    private final GatewayConnector gatewayConnector;

    private CoapServer coapServer;

    @Override
    public void start(String gateBrokerUrl, String gatewayId, String topic) {
        if (coapServer == null) {
            coapServer = new CoapServer(5683);
            coapServer.start();
        }

        // gatewayId 중복 체크
        if (coapServer.getRoot().getChild(gatewayId) == null) {
            coapServer.add(new DataResource(gatewayId, topic));
        }

        coapServer.add(new DataResource(gatewayId, topic));
        log.info("[CoAP 수신기] CoAP 서버 시작 및 리소스 등록 - {}", gatewayId);
    }


    @Getter
    private class DataResource extends CoapResource {
        private final String gatewayId;
        private final String topic;

        public DataResource(String gatewayId, String topic) {
            super(gatewayId); // ex) /coap-1
            this.gatewayId = gatewayId;
            this.topic = topic;
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            String payload = exchange.getRequestText();
            log.info("[CoAP] 데이터 수신 - gatewayId: {}, payload: {}", gatewayId, payload);

            try {
                // 예: "time=123456,value=78.9"
                Map<String, String> map = Arrays.stream(payload.split(","))
                        .map(s -> s.split("="))
                        .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));

                long time = Long.parseLong(map.get("time"));
                double value = Double.parseDouble(map.get("value"));

                DataRequest request = new DataRequest(topic, time, value);
                gatewayConnector.receiveGatewayData(topic, request);
                exchange.respond(CoAP.ResponseCode.CONTENT, "OK");

            } catch (Exception e) {
                log.error("CoAP 데이터 처리 실패", e);
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Invalid payload");
            }
        }
    }
}
