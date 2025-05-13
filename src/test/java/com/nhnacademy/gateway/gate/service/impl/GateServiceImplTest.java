package com.nhnacademy.gateway.gate.service.impl;

import com.nhnacademy.gateway.event.dto.EventCreateRequest;
import com.nhnacademy.gateway.event.rabbitMq.EventProducer;
import com.nhnacademy.gateway.gate.domain.Gate;
import com.nhnacademy.gateway.gate.dto.GateRequest;
import com.nhnacademy.gateway.gate.exception.ConflictException;
import com.nhnacademy.gateway.gate.exception.GatewayNotFoundException;
import com.nhnacademy.gateway.gate.repository.GateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GateServiceImplTest {

    @Mock
    private GateRepository gateRepository;

    @Mock
    private EventProducer eventProducer;

    @InjectMocks
    private GateServiceImpl gateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateGate_duplicateAddress() {
        GateRequest request = new GateRequest("G1", "MQTT", "127.0.0.1", 1883, "desc");
        when(gateRepository.existsByBrokerIpAndPort("127.0.0.1", 1883)).thenReturn(true);

        assertThatThrownBy(() -> gateService.createGate(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("이미 사용 중인 주소입니다.");

        verify(eventProducer).sendEvent(any(EventCreateRequest.class));
    }

    @Test
    void testChangeActivate_success() {
        Long gateNo = 1L;
        Gate gate = mock(Gate.class);
        when(gateRepository.findById(gateNo)).thenReturn(Optional.of(gate));

        gateService.changeActivate(gateNo);

        verify(gate).changeIsActive(true);
        verify(gateRepository).save(gate);
        verify(eventProducer).sendEvent(any(EventCreateRequest.class));
    }

    @Test
    void testChangeInactivate_success() {
        Long gateNo = 2L;
        Gate gate = mock(Gate.class);
        when(gateRepository.findById(gateNo)).thenReturn(Optional.of(gate));

        gateService.changeInactivate(gateNo);

        verify(gate).changeIsActive(false);
        verify(gateRepository).save(gate);
        verify(eventProducer).sendEvent(any(EventCreateRequest.class));
    }

    @Test
    void testChangeThresholdStatus_success() {
        Long gateNo = 3L;
        Gate gate = mock(Gate.class);
        when(gateRepository.findById(gateNo)).thenReturn(Optional.of(gate));

        gateService.changeThresholdStatus(gateNo);

        verify(gate).changeThresholdStatus(true);
        verify(gateRepository).save(gate);
        verify(eventProducer).sendEvent(any(EventCreateRequest.class));
    }

    @Test
    void testChangeActivate_notFound() {
        Long gateNo = 100L;
        when(gateRepository.findById(gateNo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gateService.changeActivate(gateNo))
                .isInstanceOf(GatewayNotFoundException.class);

        verify(eventProducer).sendEvent(any(EventCreateRequest.class));
    }

    @Test
    void testChangeInactivate_notFound() {
        Long gateNo = 101L;
        when(gateRepository.findById(gateNo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gateService.changeInactivate(gateNo))
                .isInstanceOf(GatewayNotFoundException.class);

        verify(eventProducer).sendEvent(any(EventCreateRequest.class));
    }

    @Test
    void testChangeThresholdStatus_notFound() {
        Long gateNo = 102L;
        when(gateRepository.findById(gateNo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gateService.changeThresholdStatus(gateNo))
                .isInstanceOf(GatewayNotFoundException.class);

        verify(eventProducer).sendEvent(any(EventCreateRequest.class));
    }
}