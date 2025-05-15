package com.nhnacademy.gateway.gate.service.impl;

import com.nhnacademy.gateway.event.dto.EventCreateRequest;
import com.nhnacademy.gateway.event.rabbitmq.EventProducer;
import com.nhnacademy.gateway.gate.domain.Gate;
import com.nhnacademy.gateway.gate.dto.GateRequest;
import com.nhnacademy.gateway.gate.dto.GateResponse;
import com.nhnacademy.gateway.gate.dto.GateSummaryResponse;
import com.nhnacademy.gateway.exception.ConflictException;
import com.nhnacademy.gateway.exception.GatewayNotFoundException;
import com.nhnacademy.gateway.gate.repository.GateRepository;
import com.nhnacademy.gateway.user.common.UserContextHolder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GateServiceImplTest {

    @Mock
    private GateRepository gateRepository;

    @Mock
    private EventProducer eventProducer;

    @InjectMocks
    private GateServiceImpl gateService;

    private static final String DEPT = "test-dept";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        UserContextHolder.setDepartmentId(DEPT);
    }

    @Test
    void testCreateGate_success() {
        GateRequest req = new GateRequest("G1","MQTT","127.0.0.1",1883,"desc");
        assertEquals("G1", req.getGateName());

        when(gateRepository.existsByBrokerIpAndPort("127.0.0.1",1883)).thenReturn(false);

        // savedGate이 반환되도록 gateNo 주입
        Gate toSave = Gate.ofNewGate("G1","MQTT","127.0.0.1",1883,DEPT,"desc");
        // reflection으로 gateNo 설정
        try {
            var f = Gate.class.getDeclaredField("gateNo");
            f.setAccessible(true);
            f.set(toSave, 1L);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        when(gateRepository.save(any(Gate.class))).thenReturn(toSave);

        Long result = gateService.createGate(req);

        assertThat(result).isEqualTo(1L);
        verify(eventProducer).sendEvent(any(EventCreateRequest.class));
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
    void testGetGate_success() {
        // 준비
        GateResponse expected = new GateResponse(
                1L,"G1","MQTT","127.0.0.1",1883,
                "client","dept","desc",
                true,false,
                LocalDateTime.now(), LocalDateTime.now());
        assertEquals(1L, expected.getGateNo());

        when(gateRepository.findById(1L))
                .thenReturn(Optional.of(Gate.ofNewGate(
                        expected.getGateName(),expected.getProtocol(),
                        expected.getBrokerIp(), expected.getPort(),
                        expected.getDepartmentId(), expected.getDescription()
                )));
        // 실제로 gateMapper 통해 응답만 테스트하므로 mock 내부 mapping 생략…

        GateResponse actual = gateService.getGate(1L);
        assertThat(actual.getGateName()).isEqualTo(expected.getGateName());
    }

    @Test
    void testGetGate_notFound() {
        when(gateRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gateService.getGate(1L))
                .isInstanceOf(GatewayNotFoundException.class);
    }

    @Test
    void testGetGateList() {
        GateSummaryResponse summary = new GateSummaryResponse(
                1L,"G1","MQTT", true, false);

        assertEquals(1L, summary.getGateNo());
        assertEquals("G1", summary.getGateName());
        assertEquals("MQTT", summary.getProtocol());
        assertTrue(summary.isActive());
        assertFalse(summary.isThresholdStatus());

        when(gateRepository.findGateSummaries())
                .thenReturn(List.of(summary));

        List<GateSummaryResponse> list = gateService.getGateList();
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getGateName()).isEqualTo("G1");
    }

    @Test
    void testUpdateGate_changeAllFields_success() {
        Long id = 1L;
        Gate gate = Gate.ofNewGate("G1", "MQTT", "127.0.0.1", 1883, DEPT, "desc");

        try {
            var f = Gate.class.getDeclaredField("gateNo");
            f.setAccessible(true);
            f.set(gate, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Assertions.fail("Reflection failed: " + e.getMessage());
        }

        when(gateRepository.findById(id)).thenReturn(Optional.of(gate));
        GateRequest req = new GateRequest("G2", "MQTT", "192.168.0.1", 1884, "updated description");

        boolean result = gateService.updateGate(id, req);

        assertThat(result).isTrue(); // 중요한 필드 바뀜 → resetNeeded = true

        assertThat(gate.getGateName()).isEqualTo("G2");
        assertThat(gate.getBrokerIp()).isEqualTo("192.168.0.1");
        assertThat(gate.getPort()).isEqualTo(1884);
        assertThat(gate.getDescription()).isEqualTo("updated description");

        verify(gateRepository).save(gate);
        verify(eventProducer).sendEvent(any(EventCreateRequest.class));
    }

    @Test
    void testUpdateGate_noChangeFields_success() {
        Long id = 1L;
        Gate gate = Gate.ofNewGate("G1", "MQTT", "127.0.0.1", 1883, DEPT, "desc");

        try {
            var f = Gate.class.getDeclaredField("gateNo");
            f.setAccessible(true);
            f.set(gate, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Assertions.fail("Reflection failed: " + e.getMessage());
        }

        when(gateRepository.findById(id)).thenReturn(Optional.of(gate));
        GateRequest req = new GateRequest("G1", "MQTT", "127.0.0.1", 1883, "desc");

        boolean result = gateService.updateGate(id, req);

        assertThat(result).isFalse(); // 중요한 필드 안 바뀜 → resetNeeded = false

        verify(gateRepository).save(gate); // save는 항상 호출됨
        verify(eventProducer).sendEvent(any(EventCreateRequest.class)); // 이벤트도 항상 전송됨
    }



    @Test
    void testUpdateGate_notFound() {
        when(gateRepository.findById(anyLong())).thenReturn(Optional.empty());
        GateRequest req = new GateRequest("G2","MQTT","192.168.0.1",1884,"upd");

        assertThatThrownBy(() -> gateService.updateGate(1L, req))
                .isInstanceOf(GatewayNotFoundException.class);
    }

    @Test
    void testDeleteGate_success() {
        Long id = 2L;
        Gate gate = Gate.ofNewGate("g","CoAP","1.1.1.1",5683,DEPT,"d");
        try {
            var f = Gate.class.getDeclaredField("gateNo");
            f.setAccessible(true);
            f.set(gate, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Assertions.fail("리플렉션으로 gateNo 설정 실패: " + e.getMessage());
        }

        when(gateRepository.findById(id)).thenReturn(Optional.of(gate));

        gateService.deleteGate(id);

        verify(gateRepository).delete(gate);
        verify(eventProducer).sendEvent(any(EventCreateRequest.class));
    }

    @Test
    void testDeleteGate_notFound() {
        when(gateRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gateService.deleteGate(99L))
                .isInstanceOf(GatewayNotFoundException.class);
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