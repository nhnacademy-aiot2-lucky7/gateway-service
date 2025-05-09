package com.nhnacademy.gateway.gate.advice;

import com.nhnacademy.gateway.gate.exception.CommonHttpException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CommonAdvice {

    @ExceptionHandler(CommonHttpException.class)
    public ResponseEntity<ErrorResponse> handleCommonHttpException(CommonHttpException e) {
        log.warn("Handled CommonHttpException: {}", e.getMessage());
        return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getMessage(), e.getStatusCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(Exception e) {
        log.error("Unhandled Exception", e);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("서버 내부 오류가 발생했습니다.", 500));
    }

    @Getter
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
        private int statusCode;
    }
}