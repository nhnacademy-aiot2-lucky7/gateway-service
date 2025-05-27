package com.nhnacademy.gateway.common.enums;

public enum CoreBrokerTransportMode {

    TCP,
    SSL_TLS;

    public boolean isSecure() {
        return this == SSL_TLS;
    }
}
