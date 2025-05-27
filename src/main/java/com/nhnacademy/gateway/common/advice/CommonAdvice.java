package com.nhnacademy.gateway.common.advice;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.nhnacademy.gateway.common.exception.CommonHttpException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class CommonAdvice {

    private final PropertyNamingStrategies.NamingBase namingBase;

    public CommonAdvice(PropertyNamingStrategy namingStrategy) {
        this.namingBase = (namingStrategy instanceof PropertyNamingStrategies.NamingBase namingBase)
                ? namingBase
                : null;
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<CommonErrorResponse> bindExceptionHandler(
            BindException e,
            HttpServletRequest request
    ) {
        List<String> errors = new ArrayList<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                errors.add(
                        "{\"field\":\"%s\", \"rejected_value\":\"%s\", \"message\":\"%s\"}".formatted(
                                namingBase != null
                                        ? namingBase.translate(fieldError.getField())
                                        : fieldError.getField(),
                                String.valueOf(fieldError.getRejectedValue()),
                                fieldError.getDefaultMessage()
                        )
                );
            }
        });

        return ResponseEntity
                .badRequest()
                .body(CommonErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        String.join(", ", errors),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(CommonHttpException.class)
    public ResponseEntity<CommonErrorResponse> exceptionHandler(
            CommonHttpException e,
            HttpServletRequest request
    ) {
        String path = request.getRequestURI();
        log.warn("path({}): {}", path, e.getMessage(), e);

        return ResponseEntity
                .status(e.getStatusCode())
                .body(CommonErrorResponse.of(
                        e.getStatusCode(),
                        e.getMessage(),
                        path
                ));
    }
}
