package com.nhnacademy.gateway.gate.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "gates")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Gate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gate_no")
    @Comment("게이트웨이 번호")
    private Long gateNo;

    @Column(name = "gate_name", length = 30, nullable = false)
    @Comment("게이트웨이 이름")
    private String gateName;

    @Column(name = "gate_protocol", length = 10, nullable = false)
    @Comment("게이트웨이 통신방식")
    private String protocol;

    @Column(name = "gate_ip", length = 50, nullable = false)
    @Comment("게이트웨이 브로커 IP 주소")
    private String brokerIp;

    @Column(name = "gate_port", nullable = false)
    @Comment("게이트웨이 포트 번호")
    private Integer port;

    @Column(name = "gate_client_id", length = 36, nullable = false)
    @Comment("게이트웨이 클라이언트 아이디")
    private String clientId;

    @Column(name = "gate_department_id", length = 50, nullable = false)
    @Comment("게이트웨이 추가 부서")
    private String departmentId;

    @Column(name = "gate_description", length = 100)
    @Comment("게이트웨이 설명")
    private String description;

    @Column(name = "gate_is_active", nullable = false)
    @Comment("게이트웨이 활성화 상태")
    private boolean isActive;

    @Column(name = "gate_threshold_status", nullable = false)
    @Comment("게이트웨이 임계치 활성화 상태")
    private boolean thresholdStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Gate(String gateName, String protocol, String brokerIp, Integer port, String clientId, String departmentId, String description, boolean isActive, boolean thresholdStatus) {
        this.gateName = gateName;
        this.protocol = protocol;
        this.brokerIp = brokerIp;
        this.port = port;
        this.clientId = clientId;
        this.departmentId = departmentId;
        this.description = description;
        this.isActive = isActive;
        this.thresholdStatus = thresholdStatus;
    }

    public static Gate ofNewGate(String gateName, String protocol, String brokerIp, Integer port, String departmentId, String description) {
        return new Gate(
                gateName,
                protocol,
                brokerIp,
                port,
                UUID.randomUUID().toString(),
                departmentId,
                description,
                false,
                false
        );
    }

    public void updateGate(String gateName, String protocol, String brokerIp, Integer port, String description) {
        this.gateName = gateName;
        this.protocol = protocol;
        this.brokerIp = brokerIp;
        this.port = port;
        this.description = description;
        this.isActive = false;
        this.thresholdStatus = false;
    }

    public void changeIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void changeThresholdStatus(boolean thresholdStatus) {
        this.thresholdStatus = thresholdStatus;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this. updatedAt = LocalDateTime.now();
    }
}