package com.nhnacademy.gateway.exception;


public class MissingHeaderException extends CommonHttpException {

    private static final int HTTP_STATUS_CODE = 400;

    public MissingHeaderException() {
        super(HTTP_STATUS_CODE, "필수 요청 헤더가 누락되었습니다.");
    }

    public MissingHeaderException(String message) {
        super(HTTP_STATUS_CODE, message);
    }
}