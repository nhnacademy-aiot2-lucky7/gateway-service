package com.nhnacademy.gateway.gateway_info.domain;

import com.nhnacademy.gateway.common.enums.IoTProtocol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "gateways")
@Getter
public class Gateway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gateway_id", nullable = false)
    @Comment("게이트웨이_아이디")
    private Long gatewayId;

    @Column(name = "gateway_address", length = 100, nullable = false)
    private String address;

    @Column(name = "gateway_port", nullable = false)
    private Integer port;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "protocol", length = 30, nullable = false)
    private IoTProtocol protocol;

    @Column(name = "gateway_name", length = 50, nullable = false)
    private String gatewayName;

    @Column(name = "client_id", length = 50, nullable = false, updatable = false)
    private String clientId;

    @Column(name = "department_id", length = 50, nullable = false)
    private String departmentId;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "sensor_count", nullable = false)
    private Integer sensorCount;

    @Column(name = "threshold_status", columnDefinition = "tinyint")
    private Boolean thresholdStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Gateway() {
    }

    private Gateway(
            String address, Integer port, IoTProtocol protocol,
            String gatewayName, String clientId, String departmentId,
            String description, Integer sensorCount, Boolean thresholdStatus
    ) {
        this.address = address;
        this.port = port;
        this.protocol = protocol;
        this.gatewayName = gatewayName;
        this.clientId = clientId;
        this.departmentId = departmentId;
        this.description = description;
        this.sensorCount = sensorCount;
        this.thresholdStatus = thresholdStatus;
    }

    public static Gateway ofNewGateway(
            String address, Integer port, IoTProtocol protocol,
            String gatewayName, String clientId, String departmentId,
            String description, Integer sensorCount, Boolean thresholdStatus
    ) {
        return new Gateway(
                address,
                port,
                protocol,
                gatewayName,
                clientId,
                departmentId,
                description,
                sensorCount,
                thresholdStatus
        );
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
