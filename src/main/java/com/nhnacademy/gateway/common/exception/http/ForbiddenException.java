package com.nhnacademy.gateway.common.exception.http;

import com.nhnacademy.gateway.common.exception.CommonHttpException;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends CommonHttpException {

    private static final int HTTP_STATUS_CODE = HttpStatus.FORBIDDEN.value();

    public ForbiddenException() {
        this(HttpStatus.FORBIDDEN.getReasonPhrase());
    }

    public ForbiddenException(final String message) {
        this(message, null);
    }

    public ForbiddenException(final String message, final Throwable cause) {
        super(HTTP_STATUS_CODE, message, cause);
    }
}
