package com.nhnacademy.gateway.common.properties;

import com.nhnacademy.gateway.common.enums.CoreBrokerTransportMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "core-broker-sender")
public final class CoreBrokerProperties {

    private CoreBrokerTransportMode transportMode = CoreBrokerTransportMode.TCP;

    private String address = "localhost";

    private int port = 1883;

    private String clientId = "core-broker-sender";

    private String topic = "project-data/";

    private Integer qos = 1;

    public boolean isSecure() {
        return transportMode.isSecure();
    }
}
