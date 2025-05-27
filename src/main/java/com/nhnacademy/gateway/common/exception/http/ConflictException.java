package com.nhnacademy.gateway.common.exception.http;

import com.nhnacademy.gateway.common.exception.CommonHttpException;
import org.springframework.http.HttpStatus;

public class ConflictException extends CommonHttpException {

    private static final int HTTP_STATUS_CODE = HttpStatus.CONFLICT.value();

    public ConflictException() {
        this("Conflict with existing resource");
    }

    public ConflictException(final String message) {
        this(message, null);
    }

    public ConflictException(final String message, final Throwable cause) {
        super(HTTP_STATUS_CODE, message, cause);
    }
}
