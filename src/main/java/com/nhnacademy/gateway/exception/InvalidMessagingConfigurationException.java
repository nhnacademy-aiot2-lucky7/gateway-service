package com.nhnacademy.gateway.exception;


public class InvalidMessagingConfigurationException extends CommonHttpException {

    private static final int HTTP_STATUS_CODE = 500;

    public InvalidMessagingConfigurationException() {
        super(HTTP_STATUS_CODE, "RabbitMQ 설정이 누락되었거나 잘못되었습니다.");
    }

    public InvalidMessagingConfigurationException(String message) {
        super(HTTP_STATUS_CODE, message);
    }
}