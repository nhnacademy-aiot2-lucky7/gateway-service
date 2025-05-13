package com.nhnacademy.gateway.exception;

public class UserNotFoundException extends CommonHttpException {

    private static final int STATUS_CODE = 404;

    public UserNotFoundException() {
        super(STATUS_CODE, "헤더가 올바르지 않음");
    }

    /**
     * 사용자 정보를 찾을 수 없을 때 예외를 생성합니다.
     */
    public UserNotFoundException(String message) {
        super(STATUS_CODE, message);
    }

}