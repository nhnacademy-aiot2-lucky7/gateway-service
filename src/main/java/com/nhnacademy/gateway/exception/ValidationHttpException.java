package com.nhnacademy.gateway.exception;

public class ValidationHttpException extends CommonHttpException {

  private static final int HTTP_STATUS_CODE = 400;

  public ValidationHttpException() {
    super(HTTP_STATUS_CODE, "잘못된 요청입니다.");
  }

  public ValidationHttpException(String message) {
    super(HTTP_STATUS_CODE, message);
  }
}
