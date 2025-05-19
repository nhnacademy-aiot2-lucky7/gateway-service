package com.nhnacademy.gateway.gate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.gateway.gate.dto.GateRequest;
import com.nhnacademy.gateway.gate.dto.GateResponse;
import com.nhnacademy.gateway.gate.dto.GateSummaryResponse;
import com.nhnacademy.gateway.gate.service.GateService;
import com.nhnacademy.gateway.mqtt.client.GatewayConnector;
import com.nhnacademy.gateway.user.adaptor.UserAdaptor;
import com.nhnacademy.gateway.user.dto.DepartmentResponse;
import com.nhnacademy.gateway.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GateController.class)
class GateControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    GateService gateService;

    @MockitoBean
    GatewayConnector connector;

    @MockitoBean
    UserAdaptor userAdaptor;

    GateRequest gateRequest;
    GateResponse gateResponse;
    GateSummaryResponse gateSummaryResponse;

    @BeforeEach
    void setUp() {
        DepartmentResponse departmentResponse = new DepartmentResponse("dept-01", "Department Name");
        UserResponse userResponse = new UserResponse(
                "ROLE_USER",
                1L,
                "John Doe",
                "johndoe@example.com",
                "123-456-7890",
                departmentResponse,
                null
        );
        when(userAdaptor.getUserInfo(anyString())).thenReturn(ResponseEntity.ok(userResponse));

        gateRequest = new GateRequest("Main Gate", "MQTT", "192.168.0.1", 1883, "Main entrance gateway");

        gateResponse = new GateResponse(
                1L, "Main Gate", "MQTT", "192.168.0.1", 1883, "client-01",
                "dept-01", "Main entrance gateway", true, false,
                LocalDateTime.now(), LocalDateTime.now()
        );

        gateSummaryResponse = new GateSummaryResponse(1L, "Main Gate", "MQTT", true, false);
    }

    @Test
    void createGateTest() throws Exception {
        when(gateService.createGate(any(GateRequest.class))).thenReturn(1L);
        doNothing().when(connector).startGateway(anyString(), anyString(), anyString());
        doNothing().when(gateService).changeActivate(anyLong());

        mockMvc.perform(post("/gates/connect")
                        .header("X-User-Id", "user-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gateRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    @Test
    void getGateTest() throws Exception {
        when(gateService.getGate(1L)).thenReturn(gateResponse);

        mockMvc.perform(get("/gates/{gateNo}", 1L)
                        .header("X-User-Id", "user-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gateNo").value(1L))
                .andExpect(jsonPath("$.gateName").value("Main Gate"));
    }

    @Test
    void getGateListTest() throws Exception {
        when(gateService.getGateList()).thenReturn(List.of(gateSummaryResponse));

        mockMvc.perform(get("/gates/list")
                        .header("X-User-Id", "user-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].gateNo").value(1L));
    }

    @Test
    void getGateListNoContentTest() throws Exception {
        when(gateService.getGateList()).thenReturn(List.of());

        mockMvc.perform(get("/gates/list")
                        .header("X-User-Id", "user-01"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateGateTest_withReset() throws Exception {
        when(gateService.updateGate(anyLong(), any(GateRequest.class))).thenReturn(true);
        doNothing().when(connector).shutdown(anyString());
        doNothing().when(connector).startGateway(anyString(), anyString(), anyString());

        mockMvc.perform(put("/gates/{gateNo}", 1L)
                        .header("X-User-Id", "user-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updateGateTest_withoutReset() throws Exception {
        when(gateService.updateGate(anyLong(), any(GateRequest.class))).thenReturn(false);

        mockMvc.perform(put("/gates/{gateNo}", 1L)
                        .header("X-User-Id", "user-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gateRequest)))
                .andExpect(status().isOk());

        verify(connector, never()).shutdown(anyString());
        verify(connector, never()).startGateway(anyString(), anyString(), anyString());
    }

    @Test
    void deleteGateTest() throws Exception {
        doNothing().when(connector).shutdown(anyString());
        doNothing().when(gateService).deleteGate(anyLong());

        mockMvc.perform(delete("/gates/{gateNo}", 1L)
                        .header("X-User-Id", "user-01"))
                .andExpect(status().isNoContent());
    }

    @Test
    void changeActivateTest() throws Exception {
        when(gateService.getGate(1L)).thenReturn(gateResponse);
        doNothing().when(connector).startGateway(anyString(), anyString(), anyString());
        doNothing().when(gateService).changeActivate(anyLong());

        mockMvc.perform(patch("/gates/{gateNo}/activate", 1L)
                        .header("X-User-Id", "user-01"))
                .andExpect(status().isOk());
    }

    @Test
    void changeInactivateTest() throws Exception {
        doNothing().when(connector).shutdown(anyString());
        doNothing().when(gateService).changeInactivate(anyLong());

        mockMvc.perform(patch("/gates/{gateNo}/inactivate", 1L)
                        .header("X-User-Id", "user-01"))
                .andExpect(status().isOk());
    }

    @Test
    void changeThresholdStatusTest() throws Exception {
        doNothing().when(gateService).changeThresholdStatus(anyLong());

        mockMvc.perform(patch("/gates/{gateNo}/threshold", 1L)
                        .header("X-User-Id", "user-01"))
                .andExpect(status().isOk());
    }

    @Test
    void getDepartmentIdTest() throws Exception {
        when(gateService.getGate(1L)).thenReturn(gateResponse);

        mockMvc.perform(get("/gates/{gateNo}/department", 1L)
                        .header("X-User-Id", "user-01"))
                .andExpect(status().isOk())
                .andExpect(content().string("dept-01"));
    }
}
