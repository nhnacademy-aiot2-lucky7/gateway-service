package com.nhnacademy.gateway.exception;


public class FeignHttpException extends CommonHttpException {
    private static final int HTTP_STATUS_CODE = 502;

    public FeignHttpException() {
        super(HTTP_STATUS_CODE, "외부 API 호출 중 오류가 발생했습니다.");
    }

    public FeignHttpException(String message) {
        super(HTTP_STATUS_CODE, message);
    }
}