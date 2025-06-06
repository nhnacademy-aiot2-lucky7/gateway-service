package com.nhnacademy.gateway.common.exception.http;

import com.nhnacademy.gateway.common.exception.CommonHttpException;
import org.springframework.http.HttpStatus;

public class BadRequestException extends CommonHttpException {

    private static final int HTTP_STATUS_CODE = HttpStatus.BAD_REQUEST.value();

    public BadRequestException() {
        this(HttpStatus.BAD_REQUEST.getReasonPhrase());
    }

    public BadRequestException(final String message) {
        this(message, null);
    }

    public BadRequestException(final String message, final Throwable cause) {
        super(HTTP_STATUS_CODE, message, cause);
    }
}
