package com.nhnacademy.gateway.mqtt.receivedata.receiver.impl;

import com.nhnacademy.gateway.mqtt.receivedata.receiver.GatewayReceiver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HttpDataReceiver implements GatewayReceiver {

    @Override
    public void start(String gateBrokerUrl, String gatewayId, String topic) {

        log.info("[HTTP 수신기] 별도 초기화 없음: {}", gatewayId);
    }
}
