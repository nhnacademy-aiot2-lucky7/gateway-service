package com.nhnacademy.gateway.common.exception.http.extend;

import com.nhnacademy.gateway.common.exception.http.ConflictException;

public class GatewayAlreadyExistsException extends ConflictException {

    public GatewayAlreadyExistsException(String ipAddress, int port) {
        this(ipAddress, port, null);
    }

    public GatewayAlreadyExistsException(String ipAddress, int port, Throwable cause) {
        super("gateway already exists: {%s:%d}".formatted(ipAddress, port));
    }
}
