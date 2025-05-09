package com.nhnacademy.gateway.gate.exception;

import lombok.Getter;

/**
 * HTTP 상태 코드와 메시지를 함께 포함할 수 있는 사용자 정의 예외 클래스입니다.
 * <p>
 * 주로 HTTP 오류 응답을 처리할 때 사용되며, 상태 코드와 예외 메시지를 함께 전달할 수 있습니다.
 */
@Getter
public class CommonHttpException extends RuntimeException {

    /**
     * HTTP 상태 코드 (예: 400, 404, 500 등)
     * -- GETTER --
     *  저장된 HTTP 상태 코드를 반환합니다.
     *
     * @return HTTP 상태 코드

     */
    private final int statusCode;

    /**
     * 상태 코드와 메시지를 기반으로 예외 객체를 생성합니다.
     *
     * @param statusCode HTTP 상태 코드
     * @param message    예외 메시지
     */
    public CommonHttpException(final int statusCode, final String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * 상태 코드, 메시지, 원인 예외(cause)를 기반으로 예외 객체를 생성합니다.
     *
     * @param statusCode HTTP 상태 코드
     * @param message    예외 메시지
     * @param cause      원인 예외
     */
    public CommonHttpException(final int statusCode, final String message, final Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

}