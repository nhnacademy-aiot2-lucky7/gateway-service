package com.nhnacademy.gateway.mqtt.receivedata.receiver.controller;

import com.nhnacademy.gateway.mqtt.receivedata.dto.DataRequest;
import com.nhnacademy.gateway.mqtt.client.GatewayConnector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gateway")
@RequiredArgsConstructor
@Slf4j
public class HttpDataController {

    private final GatewayConnector gatewayConnector;

    /**
     * HTTP 방식으로 수신한 센서 데이터 처리
     */
    @PostMapping("/{gatewayId}/data")
    public ResponseEntity<Void> receiveData(
            @PathVariable String gatewayId,
            @RequestBody DataRequest request) {

        log.info("HTTP 게이트웨이 데이터 수신 - gatewayId: {}, data: {}", gatewayId, request);

        String topic = gatewayId + "/data";

        // 새롭게 DataRequest 객체 생성
        DataRequest newRequest = new DataRequest(topic, request.getTime(), request.getValue());

        // GatewayConnector에 전달
        gatewayConnector.receiveGatewayData(topic, newRequest);

        return ResponseEntity.ok().build();
    }
}