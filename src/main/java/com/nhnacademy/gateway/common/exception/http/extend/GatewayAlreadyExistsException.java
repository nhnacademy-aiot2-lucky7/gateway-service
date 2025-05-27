package com.nhnacademy.gateway.common.exception.http.extend;

import com.nhnacademy.gateway.common.exception.http.ConflictException;

public class GatewayAlreadyExistsException extends ConflictException {

    public GatewayAlreadyExistsException() {
        this(null);
    }

    public GatewayAlreadyExistsException(Throwable cause) {
        super("A gateway with the same IP address and port already exists.", cause);
    }
}
