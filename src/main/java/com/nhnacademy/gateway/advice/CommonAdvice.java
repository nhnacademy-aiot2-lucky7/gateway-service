package com.nhnacademy.gateway.advice;

import com.nhnacademy.gateway.exception.CommonHttpException;
import com.nhnacademy.gateway.exception.ValidationHttpException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

@Slf4j
@RestControllerAdvice
public class CommonAdvice {

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public void handleValidationExceptions(Exception e) {
        BindingResult bindingResult = e instanceof MethodArgumentNotValidException methodEx
                ? methodEx.getBindingResult()
                : ((BindException) e).getBindingResult();

        FieldError fieldError = bindingResult.getFieldError();
        String errorMessage = fieldError != null
                ? String.format("필드 '%s': %s", fieldError.getField(), fieldError.getDefaultMessage())
                : "유효성 검사 실패";

        // 핵심: 변환해서 던짐
        throw new ValidationHttpException(errorMessage);
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