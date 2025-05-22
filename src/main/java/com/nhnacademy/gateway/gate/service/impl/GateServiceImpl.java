package com.nhnacademy.gateway.gate.service.impl;

import com.nhnacademy.gateway.event.dto.EventCreateRequest;
import com.nhnacademy.gateway.event.rabbitmq.EventProducer;
import com.nhnacademy.gateway.user.common.UserContextHolder;
import com.nhnacademy.gateway.gate.domain.Gate;
import com.nhnacademy.gateway.gate.dto.GateRequest;
import com.nhnacademy.gateway.gate.dto.GateResponse;
import com.nhnacademy.gateway.gate.dto.GateSummaryResponse;
import com.nhnacademy.gateway.exception.ConflictException;
import com.nhnacademy.gateway.exception.GatewayNotFoundException;
import com.nhnacademy.gateway.gate.repository.GateRepository;
import com.nhnacademy.gateway.gate.service.GateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class GateServiceImpl implements GateService {

    private final GateRepository gateRepository;
    private final EventProducer eventProducer;

    @Override
    public Long createGate(GateRequest gateRegisterRequest) {
        log.debug("게이트웨이 등록 시작! 게이트웨이 정보: {}", gateRegisterRequest);

        String departmentId = UserContextHolder.getDepartmentId();

        boolean isExistAddress = gateRepository.existsByBrokerIpAndPort(
                gateRegisterRequest.getBrokerIp(),
                gateRegisterRequest.getPort()
        );

        if (isExistAddress) {
            sendErrorEvent(null, departmentId, "중복된 게이트웨이 주소");
            throw new ConflictException("이미 사용 중인 주소입니다.");
        }

        Gate gate = Gate.ofNewGate(
                gateRegisterRequest.getGateName(),
                gateRegisterRequest.getProtocol(),
                gateRegisterRequest.getBrokerIp(),
                gateRegisterRequest.getPort(),
                departmentId,
                gateRegisterRequest.getDescription()
        );

        Gate savedGate = gateRepository.save(gate);
        sendInfoEvent(savedGate.getGateNo(), departmentId, "새로운 게이트웨이 등록 성공");

        return savedGate.getGateNo();
    }

    @Transactional(readOnly = true)
    @Override
    public GateResponse getGate(Long gateNo) {
        log.debug("게이트웨이 조회 시작! 게이트웨이 아이디 : {}", gateNo);

        Gate gate = gateRepository.findById(gateNo)
                .orElseThrow(GatewayNotFoundException::new);

        return gateMapper(gate);
    }

    @Override
    public GateResponse getGateByAddress(String ip, int port) {
        log.debug("게이트웨이 조회 시작! 게이트웨이 아이디 : {} : {}", ip, port);

        if (!gateRepository.existsByBrokerIpAndPort(ip, port)) {
            throw new ConflictException("존재하지 않는 주소");
        }

        Gate gate = gateRepository.findByBrokerIpAndPort(ip, port);

        return gateMapper(gate);
    }

    @Transactional(readOnly = true)
    @Override
    public List<GateSummaryResponse> getGateList() {
        log.debug("모든 게이트웨이 조회 시작!");

        return gateRepository.findGateSummaries();
    }

    @Override
    public boolean updateGate(Long gateNo, GateRequest gateUpdateRequest) {
        log.debug("게이트웨이 수정 시작! 게이트웨이 아이디 : {}", gateNo);

        String departmentId = UserContextHolder.getDepartmentId();

        Gate gate = findGateOrThrowWithErrorEvent(gateNo, departmentId, "존재하지 않는 게이트웨이 수정 요청");

        boolean isExistAddress = gateRepository.existsByBrokerIpAndPort(
                gateUpdateRequest.getBrokerIp(),
                gateUpdateRequest.getPort()
        );

        if (isExistAddress) {
            sendErrorEvent(null, departmentId, "중복된 게이트웨이 주소");
            throw new ConflictException("이미 사용 중인 주소입니다.");
        }

        boolean protocolChanged = !Objects.equals(gate.getProtocol(), gateUpdateRequest.getProtocol());
        boolean ipChanged = !Objects.equals(gate.getBrokerIp(), gateUpdateRequest.getBrokerIp());
        boolean portChanged = !Objects.equals(gate.getPort(), gateUpdateRequest.getPort());

        // 게이트웨이 중요한 정보인 protocol, ip, port 중에 하나라도 바뀌면 연결부터 게이트웨이 활성화, 임계치 계산 다시 시작해야 함
        boolean resetNeeded = protocolChanged || ipChanged || portChanged;

        gate.updateGate(
                gateUpdateRequest.getGateName(),
                gateUpdateRequest.getProtocol(),
                gateUpdateRequest.getBrokerIp(),
                gateUpdateRequest.getPort(),
                gateUpdateRequest.getDescription(),
                resetNeeded
        );

        gateRepository.save(gate);

        log.debug("게이트웨이 수정 완료. 연결 재시도 필요 여부: {}", resetNeeded);

        if (resetNeeded) {
            sendInfoEvent(gateNo, departmentId, "게이트웨이 정보 수정 성공 - 연결 재시도 필요");
        } else {
            sendInfoEvent(gateNo, departmentId, "게이트웨이 정보 수정 성공 - 연결 재시도 불필요");
        }

        return resetNeeded;
    }

    @Override
    public void changeActivate(Long gateNo) {
        log.debug("게이트웨이 활성화! 게이트웨이 아이디 : {}", gateNo);

        String departmentId = UserContextHolder.getDepartmentId();

        Gate gate = findGateOrThrowWithErrorEvent(gateNo, departmentId, "존재하지 않는 게이트웨이 활성화 요청");

        gate.changeIsActive(true);

        gateRepository.save(gate);
        sendInfoEvent(gateNo, departmentId, "게이트웨이 활성화");
    }

    @Override
    public void changeInactivate(Long gateNo) {
        log.debug("게이트웨이 비활성화! 게이트웨이 아이디 : {}", gateNo);
        String departmentId = UserContextHolder.getDepartmentId();

        Gate gate = findGateOrThrowWithErrorEvent(gateNo, departmentId, "존재하지 않는 게이트웨이 비활성화 요청");

        gate.changeIsActive(false);

        gateRepository.save(gate);
        sendInfoEvent(gateNo, departmentId, "게이트웨이 비활성화");
    }

    @Override
    public void changeThresholdStatus(Long gateNo) {
        log.debug("게이트웨이 임계치 활성화! 게이트웨이 아이디 : {}", gateNo);
        String departmentId = UserContextHolder.getDepartmentId();

        Gate gate = findGateOrThrowWithErrorEvent(gateNo, departmentId, "존재하지 않는 게이트웨이 임계치 요청");

        gate.changeThresholdStatus(true);

        gateRepository.save(gate);
        sendInfoEvent(gateNo, departmentId, "게이트웨이 임계치 활성화");
    }

    @Override
    public void deleteGate(Long gateNo) {
        log.debug("게이트웨이 삭제 시작! 게이트웨이 아이디 : {}", gateNo);

        String departmentId = UserContextHolder.getDepartmentId();

        Gate gate = findGateOrThrowWithErrorEvent(gateNo, departmentId, "존재하지 않는 게이트웨이 삭제 요청");

        gateRepository.delete(gate);

        sendInfoEvent(gateNo, departmentId, "게이트웨이 삭제 성공");
    }

    private GateResponse gateMapper(Gate gate) {
        return new GateResponse(
                gate.getGateNo(),
                gate.getGateName(),
                gate.getProtocol(),
                gate.getBrokerIp(),
                gate.getPort(),
                gate.getClientId(),
                gate.getDepartmentId(),
                gate.getDescription(),
                gate.isActive(),
                gate.isThresholdStatus(),
                gate.getCreatedAt(),
                gate.getUpdatedAt()
        );
    }

    private Gate findGateOrThrowWithErrorEvent(Long gateNo, String departmentId, String message) {
        return gateRepository.findById(gateNo)
                .orElseThrow(() -> {
                    sendErrorEvent(gateNo, departmentId, message);
                    return new GatewayNotFoundException();
                });
    }

    private void sendErrorEvent(Long gateNo, String departmentId, String message) {
        EventCreateRequest event = new EventCreateRequest(
                "ERROR",
                message,
                gateNo == null ? "Null" : gateNo.toString(),
                departmentId,
                LocalDateTime.now()
        );
        eventProducer.sendEvent(event);
    }

    private void sendInfoEvent(Long gateNo, String departmentId, String message) {
        EventCreateRequest event = new EventCreateRequest(
                "INFO",
                message,
                gateNo.toString(),
                departmentId,
                LocalDateTime.now()
        );
        eventProducer.sendEvent(event);
    }
}
