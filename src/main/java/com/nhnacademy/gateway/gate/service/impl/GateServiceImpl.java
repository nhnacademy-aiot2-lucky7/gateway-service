package com.nhnacademy.gateway.gate.service.impl;

import com.nhnacademy.gateway.gate.domain.Gate;
import com.nhnacademy.gateway.gate.dto.GateRequest;
import com.nhnacademy.gateway.gate.dto.GateResponse;
import com.nhnacademy.gateway.gate.dto.GateSummaryResponse;
import com.nhnacademy.gateway.gate.exception.ConflictException;
import com.nhnacademy.gateway.gate.exception.GatewayNotFoundException;
import com.nhnacademy.gateway.gate.repository.GateRepository;
import com.nhnacademy.gateway.gate.service.GateService;
import com.nhnacademy.gateway.gate.adaptor.UserAdaptor;
import com.nhnacademy.gateway.gate.dto.UserResponse;
import com.nhnacademy.gateway.gate.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class GateServiceImpl implements GateService {

    private final GateRepository gateRepository;

    private final UserAdaptor userAdaptor;

    @Override
    public Long createGate(@RequestHeader("X-User-Id") String encryptedEmail, GateRequest gateRegisterRequest) {
        log.debug("게이트웨이 등록 시작! 게이트웨이 정보: {}", gateRegisterRequest);

        boolean isExistAddress = gateRepository.existsByBrokerIpAndPort(
                gateRegisterRequest.getBrokerIp(),
                gateRegisterRequest.getPort()
        );

        if (isExistAddress) {
            throw new ConflictException("이미 사용 중인 주소입니다.");
        }

        ResponseEntity<UserResponse> responseEntity = userAdaptor.getUserInfo(encryptedEmail);

        if (responseEntity == null || responseEntity.getBody() == null) {
            log.warn("'X-User-Id'에 대한 유저 정보 조회 실패!");
            throw new UserNotFoundException("유저 정보가 올바르지 않음");
        }

        String departmentId = responseEntity.getBody().getUserDepartment();

        Gate gate = Gate.ofNewGate(
                gateRegisterRequest.getGateName(),
                gateRegisterRequest.getProtocol(),
                gateRegisterRequest.getBrokerIp(),
                gateRegisterRequest.getPort(),
                departmentId,
                gateRegisterRequest.getDescription()
        );

        Gate savedGate = gateRepository.save(gate);

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
    public List<GateSummaryResponse> getGateList() {
        log.debug("모든 게이트웨이 조회 시작!");

        return gateRepository.findGateSummaries();
    }

    @Override
    public void updateGate(Long gateNo, GateRequest gateUpdateRequest) {
        log.debug("게이트웨이 수정 시작! 게이트웨이 아이디 : {}", gateNo);

        Gate gate = gateRepository.findById(gateNo)
                .orElseThrow(GatewayNotFoundException::new);

        gate.updateGate(
                gateUpdateRequest.getGateName(),
                gateUpdateRequest.getProtocol(),
                gateUpdateRequest.getBrokerIp(),
                gateUpdateRequest.getPort(),
                gateUpdateRequest.getDescription()
        );

        gateRepository.save(gate);
    }

    @Override
    public void changeActivate(Long gateNo) {
        log.debug("게이트웨이 활성화! 게이트웨이 아이디 : {}", gateNo);

        Gate gate = gateRepository.findById(gateNo)
                .orElseThrow(GatewayNotFoundException::new);

        gate.changeIsActive(true);

        gateRepository.save(gate);
    }

    @Override
    public void changeInactivate(Long gateNo) {
        log.debug("게이트웨이 비활성화! 게이트웨이 아이디 : {}", gateNo);

        Gate gate = gateRepository.findById(gateNo)
                .orElseThrow(GatewayNotFoundException::new);

        gate.changeIsActive(false);

        gateRepository.save(gate);
    }

    @Override
    public void changeThresholdStatus(Long gateNo) {
        log.debug("게이트웨이 임계치 활성화! 게이트웨이 아이디 : {}", gateNo);

        Gate gate = gateRepository.findById(gateNo)
                .orElseThrow(GatewayNotFoundException::new);

        gate.changeThresholdStatus(true);

        gateRepository.save(gate);
    }

    @Override
    public void deleteGate(Long gateNo) {
        log.debug("게이트웨이 삭제 시작! 게이트웨이 아이디 : {}", gateNo);

        boolean isExistGate = gateRepository.existsById(gateNo);
        if (!isExistGate) {
            throw new GatewayNotFoundException();
        }

        gateRepository.deleteById(gateNo);
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
}