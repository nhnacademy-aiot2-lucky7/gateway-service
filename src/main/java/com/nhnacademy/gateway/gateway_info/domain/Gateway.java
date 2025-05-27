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
import java.util.UUID;

@Entity
@Table(name = "gateways")
@Getter
public class Gateway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gateway_no", nullable = false)
    @Comment("게이트웨이_번호")
    private Integer gatewayNo;

    @Column(name = "id_address", nullable = false)
    private String ipAddress;

    @Column(name = "gateway_port", nullable = false)
    private Integer port;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "protocol", nullable = false)
    private IoTProtocol protocol;

    @Column(name = "gateway_name", length = 50, nullable = false)
    private String gatewayName;

    @Column(name = "client_id", nullable = false, updatable = false)
    private String clientId;

    @Column(name = "department_id", nullable = false)
    private String departmentId;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "threshold_status", columnDefinition = "tinyint")
    private Boolean thresholdStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_ad")
    private LocalDateTime updatedAt;

    protected Gateway() {
    }

    private Gateway(
            String ipAddress, Integer port, IoTProtocol protocol,
            String gatewayName, String clientId, String departmentId,
            String description, Boolean thresholdStatus
    ) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.protocol = protocol;
        this.gatewayName = gatewayName;
        this.clientId = clientId;
        this.departmentId = departmentId;
        this.description = description;
        this.thresholdStatus = thresholdStatus;
    }

    public static Gateway ofNewGateway(
            String ipAddress, Integer port, IoTProtocol protocol,
            String gatewayName, String departmentId, String description
    ) {
        return new Gateway(
                ipAddress,
                port,
                protocol,
                gatewayName,
                UUID.randomUUID().toString(),
                departmentId,
                description,
                false
        );
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
