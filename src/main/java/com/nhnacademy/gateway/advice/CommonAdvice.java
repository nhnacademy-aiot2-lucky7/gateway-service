package com.nhnacademy.gateway.advice;

import com.nhnacademy.gateway.exception.CommonHttpException;
import com.nhnacademy.gateway.exception.MqttConnectionException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import feign.FeignException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

@Slf4j
@RestControllerAdvice
public class CommonAdvice {

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<CommonAdvice.ErrorResponse> handleFeignException(FeignException e) {
        log.warn("FeignException 발생: status={}, message={}", e.status(), e.getMessage());
        return ResponseEntity.status(e.status())
                .body(new ErrorResponse("Feign 오류: " + e.getMessage(), e.status()));
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public ResponseEntity<ErrorResponse> handleValidationException(Exception e) {
        BindingResult bindingResult = null;
        if (e instanceof MethodArgumentNotValidException methodEx) {
            bindingResult = methodEx.getBindingResult();
        } else if (e instanceof BindException bindEx) {
            bindingResult = bindEx.getBindingResult();
        }

        // 첫 번째 필드 오류만 추출 (필요 시 리스트로 확장 가능)
        FieldError fieldError = bindingResult.getFieldError();
        String errorMessage = fieldError != null
                ? String.format("필드 '%s': %s", fieldError.getField(), fieldError.getDefaultMessage())
                : "유효성 검사 실패";

        log.warn("Validation 실패: {}", errorMessage);

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(errorMessage, 400));
    }

    @ExceptionHandler(MqttConnectionException.class)
    public ResponseEntity<ErrorResponse> handleMqttConnectionException(MqttConnectionException e) {
        log.error("MQTT 예외 발생: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getMessage(), e.getStatusCode()));
    }

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
    @SuppressWarnings("unused")
    public static class ErrorResponse {
        private String message;
        private int statusCode;
    }
}