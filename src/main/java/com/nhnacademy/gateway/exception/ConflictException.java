package com.nhnacademy.gateway.exception;

/**
 * 리소스 간의 충돌이 발생했을 때 사용되는 예외 클래스입니다.
 * <p>
 * HTTP 상태 코드 409 (Conflict)를 나타냅니다.
 * 예: 중복된 사용자 등록, 이미 존재하는 데이터 저장 시 발생할 수 있습니다.
 */
public class ConflictException extends CommonHttpException {

    /**
     * HTTP 상태 코드 409 (Conflict)
     */
    private static final int HTTP_STATUS_CODE = 409;

    /**
     * 기본 메시지("Conflict with existing resource")를 가진 충돌 예외를 생성합니다.
     */
    public ConflictException() {
        super(HTTP_STATUS_CODE, "요청 충돌 발생");
    }

    /**
     * 사용자 정의 메시지를 가진 충돌 예외를 생성합니다.
     *
     * @param message 예외 메시지
     */
    public ConflictException(String message) {
        super(HTTP_STATUS_CODE, message);
    }
}