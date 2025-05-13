package com.nhnacademy.gateway.gate.advice;

import com.nhnacademy.gateway.advice.CommonAdvice;
import com.nhnacademy.gateway.exception.CommonHttpException;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CommonAdviceTest {

    CommonAdvice commonAdvice = new CommonAdvice();

    @Test
    @DisplayName("FeignException 예외 처리 테스트")
    void handleFeignExceptionTest() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(503);
        when(feignException.getMessage()).thenReturn("User Service Unavailable");

        ResponseEntity<CommonAdvice.ErrorResponse> response = commonAdvice.handleFeignException(feignException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Feign 오류");
        assertThat(response.getBody().getStatusCode()).isEqualTo(503);

        CommonAdvice.ErrorResponse body = response.getBody();
        assertEquals("Feign 오류: User Service Unavailable", body.getMessage());
        assertEquals(503, body.getStatusCode());
    }

    @Test
    @DisplayName("CommonHttpException 예외 처리 테스트")
    void handleCommonHttpExceptionTest() {
        CommonHttpException exception = new CommonHttpException(409, "충돌 발생");

        ResponseEntity<CommonAdvice.ErrorResponse> response = commonAdvice.handleCommonHttpException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("충돌 발생");
        assertThat(response.getBody().getStatusCode()).isEqualTo(409);
    }

    @Test
    @DisplayName("Validation 예외 처리 테스트 - MethodArgumentNotValidException")
    void handleMethodArgumentNotValidExceptionTest() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("target", "name", "must not be blank");

        when(bindingResult.getFieldError()).thenReturn(fieldError);

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<CommonAdvice.ErrorResponse> response = commonAdvice.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("필드 'name'");
        assertThat(response.getBody().getStatusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Validation 예외 처리 테스트 - BindException")
    void handleBindExceptionTest() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("target", "age", "must be positive");

        when(bindingResult.getFieldError()).thenReturn(fieldError);

        BindException exception = mock(BindException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<CommonAdvice.ErrorResponse> response = commonAdvice.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("필드 'age'");
        assertThat(response.getBody().getStatusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Unhandled Exception 처리 테스트")
    void handleUnhandledExceptionTest() {
        Exception e = new RuntimeException("알 수 없는 오류");

        ResponseEntity<CommonAdvice.ErrorResponse> response = commonAdvice.handleUnhandledException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("서버 내부 오류");
        assertThat(response.getBody().getStatusCode()).isEqualTo(500);
    }
}