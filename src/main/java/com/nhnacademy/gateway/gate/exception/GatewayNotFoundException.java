package com.nhnacademy.gateway.gate.exception;

import org.springframework.http.HttpStatus;

public class GatewayNotFoundException extends CommonHttpException {

    private static final int HTTP_STATUS_CODE = 404;

    public GatewayNotFoundException() {
        super(HTTP_STATUS_CODE, "해당 ID와 일치하는 게이트웨이를 찾을 수 없습니다.");
    }

    public GatewayNotFoundException(String message) {
        super(HTTP_STATUS_CODE, message);
    }
}