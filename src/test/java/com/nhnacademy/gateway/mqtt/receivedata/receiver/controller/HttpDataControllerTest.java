package com.nhnacademy.gateway.mqtt.receivedata.receiver.controller;

import com.nhnacademy.gateway.mqtt.client.GatewayConnector;
import com.nhnacademy.gateway.mqtt.receivedata.dto.DataRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class HttpDataControllerTest {

    @Mock
    private GatewayConnector gatewayConnector;

    @InjectMocks
    private HttpDataController httpDataController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(httpDataController).build();
    }

    @Test
    public void testReceiveData() throws Exception {
        String gatewayId = "gateway123";

        mockMvc.perform(post("/gateway/{gatewayId}/data", gatewayId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"time\": 1234567890, \"value\": 25.5}"))
                .andExpect(status().isOk());

        // 캡처 도구 생성
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<DataRequest> requestCaptor = ArgumentCaptor.forClass(DataRequest.class);

        // 실제 메서드가 호출되었는지 확인 + 인자 캡처
        verify(gatewayConnector).receiveGatewayData(topicCaptor.capture(), requestCaptor.capture());

        // 캡처된 값 확인
        assertThat(topicCaptor.getValue()).isEqualTo("gateway123/data");
        DataRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getTime()).isEqualTo(1234567890L);
        assertThat(capturedRequest.getValue()).isEqualTo(25.5);
        assertThat(capturedRequest.getTopic()).isEqualTo("gateway123/data");
    }
}