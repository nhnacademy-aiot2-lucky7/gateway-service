package com.nhnacademy.gateway.exception;

public class MqttConnectionException extends CommonHttpException {

  private static final int HTTP_STATUS_CODE = 500;

  public MqttConnectionException() {
    super(HTTP_STATUS_CODE, "Mqtt 연결 오류");
  }

  public MqttConnectionException(String message) {
    super(HTTP_STATUS_CODE, message);
  }
}