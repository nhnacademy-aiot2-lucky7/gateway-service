package com.nhnacademy.gateway.common.exception.http.extend;

import com.nhnacademy.gateway.common.exception.http.NotFoundException;

public class GatewayNotFoundException extends NotFoundException {

    public GatewayNotFoundException() {
        this(null);
    }

    public GatewayNotFoundException(Throwable cause) {
        super("gateway is not found", cause);
    }
}
