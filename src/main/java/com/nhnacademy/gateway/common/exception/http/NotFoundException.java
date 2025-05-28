package com.nhnacademy.gateway.common.exception.http;

import com.nhnacademy.gateway.common.exception.CommonHttpException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends CommonHttpException {

    private static final int HTTP_STATUS_CODE = HttpStatus.NOT_FOUND.value();

    public NotFoundException() {
        this("resource not found");
    }

    public NotFoundException(final String message) {
        this(message, null);
    }

    public NotFoundException(final String message, final Throwable cause) {
        super(HTTP_STATUS_CODE, message, cause);
    }
}
