package com.nhnacademy.gateway.gate.advice;

import com.nhnacademy.gateway.advice.CommonAdvice;
import com.nhnacademy.gateway.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommonAdviceTest {

    CommonAdvice commonAdvice = new CommonAdvice();

    @Nested
    @DisplayName("공통 예외 처리 테스트")
    class CommonHttpExceptionHandlerTest {

        @Test
        @DisplayName("FeignHttpException 처리")
        void handleFeignHttpException() {
            FeignHttpException ex = new FeignHttpException("사용자 서비스가 응답하지 않습니다");
            assertErrorResponse(commonAdvice.handleCommonHttpException(ex), HttpStatus.BAD_GATEWAY, "사용자 서비스가 응답하지 않습니다");
        }

        @Test
        @DisplayName("CommonHttpException 처리")
        void handleCommonHttpException() {
            CommonHttpException ex = new CommonHttpException(409, "충돌 발생");
            assertErrorResponse(commonAdvice.handleCommonHttpException(ex), HttpStatus.CONFLICT, "충돌 발생");
        }

        @Test
        @DisplayName("MqttConnectionException 처리")
        void handleMqttConnectionException() {
            MqttConnectionException ex = new MqttConnectionException("허브 MQTT 연결 실패");
            assertErrorResponse(commonAdvice.handleCommonHttpException(ex), HttpStatus.INTERNAL_SERVER_ERROR, "허브 MQTT 연결 실패");
        }

        @Test
        @DisplayName("ValidationHttpException 처리")
        void handleValidationHttpException() {
            ValidationHttpException ex = new ValidationHttpException("유효성 검사 실패 예외");
            assertErrorResponse(commonAdvice.handleCommonHttpException(ex), HttpStatus.BAD_REQUEST, "유효성 검사 실패 예외");
        }

        @Test
        @DisplayName("RabbitMessageSendFailedException 처리")
        void handleRabbitMessageSendFailedException() {
            RabbitMessageSendFailedException ex = new RabbitMessageSendFailedException("메시지 전송 실패");
            assertErrorResponse(commonAdvice.handleCommonHttpException(ex), HttpStatus.INTERNAL_SERVER_ERROR, "메시지 전송 실패");
        }

        @Test
        @DisplayName("RabbitConfigurationException 처리")
        void handleRabbitConfigurationException() {
            RabbitConfigurationException ex = new RabbitConfigurationException("Rabbit 설정 오류");
            assertErrorResponse(commonAdvice.handleCommonHttpException(ex), HttpStatus.INTERNAL_SERVER_ERROR, "Rabbit 설정 오류");
        }

        @Test
        @DisplayName("InvalidMessagingConfigurationException 처리")
        void handleInvalidMessagingConfigurationException() {
            InvalidMessagingConfigurationException ex = new InvalidMessagingConfigurationException("메시징 설정 오류");
            assertErrorResponse(commonAdvice.handleCommonHttpException(ex), HttpStatus.INTERNAL_SERVER_ERROR, "메시징 설정 오류");
        }
    }

    @Nested
    @DisplayName("예상치 못한 예외 처리 테스트")
    class UnhandledExceptionHandlerTest {

        @Test
        @DisplayName("Unhandled Exception 처리")
        void handleUnhandledException() {
            Exception ex = new RuntimeException("알 수 없는 오류");
            ResponseEntity<CommonAdvice.ErrorResponse> response = commonAdvice.handleUnhandledException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("서버 내부 오류");
            assertThat(response.getBody().getStatusCode()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("Validation 예외 처리 테스트")
    class ValidationExceptionHandlerTest {

        @Test
        @DisplayName("MethodArgumentNotValidException 처리")
        void handleMethodArgumentNotValidException() throws NoSuchMethodException {
            BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "objectName");
            bindingResult.addError(new FieldError("objectName", "fieldName", "필수 값입니다"));

            Method method = Dummy.class.getMethod("dummyMethod", String.class);
            MethodParameter parameter = new MethodParameter(method, 0);

            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

            ValidationHttpException thrown = assertThrows(ValidationHttpException.class, () -> {
                commonAdvice.handleValidationExceptions(ex);
            });

            assertThat(thrown.getMessage()).contains("필드 'fieldName': 필수 값입니다");
        }

        @Test
        @DisplayName("BindException 처리")
        void handleBindException() {
            BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "objectName");
            bindingResult.addError(new FieldError("objectName", "anotherField", "형식이 잘못되었습니다"));

            BindException ex = new BindException(bindingResult);

            ValidationHttpException thrown = assertThrows(ValidationHttpException.class, () -> {
                commonAdvice.handleValidationExceptions(ex);
            });

            assertThat(thrown.getMessage()).contains("필드 'anotherField': 형식이 잘못되었습니다");
        }

        // 테스트용도,  내부용 더미 메서드 (빈 메서드)
        static class Dummy {
            public void dummyMethod(String value) {
                throw new UnsupportedOperationException("테스트용 더미 메서드로 실제 호출하지 마세요.");
            }
        }
    }

    // 공통 검증 메서드
    private void assertErrorResponse(ResponseEntity<CommonAdvice.ErrorResponse> response,
                                     HttpStatus expectedStatus, String expectedMessage) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(expectedMessage);
        assertThat(response.getBody().getStatusCode()).isEqualTo(expectedStatus.value());
    }
}
