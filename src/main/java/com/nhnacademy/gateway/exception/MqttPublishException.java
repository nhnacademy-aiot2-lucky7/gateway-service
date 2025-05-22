package com.nhnacademy.gateway.exception;

public class MqttPublishException extends CommonHttpException {

    private static final int HTTP_STATUS_CODE = 502;

    public MqttPublishException() {
        super(HTTP_STATUS_CODE, "MQTT 발행 중 오류가 발생했습니다.");
    }

    public MqttPublishException(String message) {
        super(HTTP_STATUS_CODE, message);
    }
}
