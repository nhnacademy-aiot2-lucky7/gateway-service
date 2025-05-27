package com.nhnacademy.gateway.common.advice;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommonErrorResponse {

    private final LocalDateTime timestamp;

    private final Integer status;

    private final String message;

    private final String path;

    public CommonErrorResponse(Integer status, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.path = path;
    }

    public static CommonErrorResponse of(Integer status, String message, String path) {
        return new CommonErrorResponse(
                status,
                message,
                path
        );
    }
}
