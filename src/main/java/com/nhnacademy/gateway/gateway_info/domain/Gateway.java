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

/**
 * {@code Gateway} 엔티티는 IoT 게이트웨이 정보를 나타내며,
 * 주소, 포트, 프로토콜, 센서 수, 상태 등의 메타데이터를 포함합니다.
 */
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
    @Comment("게이트웨이_주소")
    private String address;

    @Column(name = "gateway_port", nullable = false)
    @Comment("게이트웨이_포트_번호")
    private Integer port;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "protocol", length = 30, nullable = false)
    @Comment("게이트웨이_프로토콜")
    private IoTProtocol protocol;

    @Column(name = "gateway_name", length = 50, nullable = false)
    @Comment("게이트웨이_이름")
    private String gatewayName;

    @Column(name = "client_id", length = 50, nullable = false, updatable = false)
    @Comment("클라이언트_아이디")
    private String clientId;

    @Column(name = "department_id", length = 50, nullable = false)
    @Comment("부서_아이디")
    private String departmentId;

    @Column(name = "description", columnDefinition = "text")
    @Comment("설명")
    private String description;

    @Column(name = "sensor_count", nullable = false)
    @Comment("게이트웨이_소속_센서_카운트")
    private Integer sensorCount;

    @Column(name = "threshold_status", columnDefinition = "tinyint")
    @Comment("게이트웨이_활성화_상태")
    private Boolean thresholdStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("생성_일자")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Comment("수정_일자")
    private LocalDateTime updatedAt;

    /**
     * JPA 전용 기본 생성자입니다.
     */
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

    public void updateGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateSensorCount(int sensorCount) {
        this.sensorCount = sensorCount;
    }

    public void updateThresholdStatusEnabled() {
        this.thresholdStatus = true;
    }

    public void updateThresholdStatusDisabled() {
        this.thresholdStatus = false;
    }
}
