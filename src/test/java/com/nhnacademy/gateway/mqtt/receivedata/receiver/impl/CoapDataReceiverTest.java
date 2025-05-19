package com.nhnacademy.gateway.mqtt.receivedata.receiver.impl;

import com.nhnacademy.gateway.mqtt.client.GatewayConnector;
import com.nhnacademy.gateway.mqtt.receivedata.dto.DataRequest;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.coap.CoAP;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoapDataReceiverTest {

    @Mock
    GatewayConnector gatewayConnector;

    CoapDataReceiver receiver;
    final String gatewayId = "gw1";
    final String topic     = "topic1";

    @BeforeEach
    void setUp() throws Exception{
        receiver = new CoapDataReceiver();

        // 리플렉션으로 gatewayConnector 필드에 @Mock 객체 주입
        Field connectorField = CoapDataReceiver.class.getDeclaredField("gatewayConnector");
        connectorField.setAccessible(true);
        connectorField.set(receiver, gatewayConnector);
    }

    @Test
    void start_multipleTimes_noExceptionAndServerInitialized() throws Exception {
        // 처음, 두 번째 호출 모두 예외 없이
        assertDoesNotThrow(() -> receiver.start("ignoredUrl", gatewayId, topic));
        assertDoesNotThrow(() -> receiver.start("ignoredUrl", gatewayId, topic));

        // coapServer 필드가 null이 아님
        Field f = CoapDataReceiver.class.getDeclaredField("coapServer");
        f.setAccessible(true);
        Object coapServer = f.get(receiver);
        assertNotNull(coapServer);
        assertTrue(coapServer instanceof CoapServer);
    }

    @Test
    void handlePOST_validPayload_callsConnectorAndRespondsOK() throws Exception {
        // 1) DataResource 클래스 리플렉션으로 가져오기
        Class<?> dataResClazz = null;
        for (Class<?> c : CoapDataReceiver.class.getDeclaredClasses()) {
            if (c == CoapDataReceiver.DataResource.class) {
                dataResClazz = c;
                break;
            }
        }
        assertNotNull(dataResClazz);

        // 2) 생성자 (outer, gatewayId, topic)
        Constructor<?> ctor =
                dataResClazz.getDeclaredConstructor(CoapDataReceiver.class, String.class, String.class);
        ctor.setAccessible(true);
        Object dataResource = ctor.newInstance(receiver, gatewayId, topic);

        // 3) handlePOST 메서드 접근
        Method handlePOST = dataResClazz.getMethod("handlePOST", CoapExchange.class);

        // 4) CoapExchange 모의
        CoapExchange exchange = mock(CoapExchange.class);
        when(exchange.getRequestText()).thenReturn("time=1000,value=55.5");

        // 5) 실행
        handlePOST.invoke(dataResource, exchange);

        // 6) connector 호출 검증
        ArgumentCaptor<DataRequest> capReq = ArgumentCaptor.forClass(DataRequest.class);
        verify(gatewayConnector).receiveGatewayData(eq(topic), capReq.capture());
        DataRequest dr = capReq.getValue();
        assertThat(dr.getTopic()).isEqualTo(topic);
        assertThat(dr.getTime()).isEqualTo(1000L);
        assertThat(dr.getValue()).isEqualTo(55.5);

        // 7) OK 응답 검증
        verify(exchange).respond(CoAP.ResponseCode.CONTENT, "OK");
    }


    @Test
    void handlePOST_invalidPayload_respondsBadRequestAndNoServiceCall() throws Exception {
        // DataResource 리플렉션 준비
        Class<?> dataResClazz = null;
        for (Class<?> c : CoapDataReceiver.class.getDeclaredClasses()) {
            if (c == CoapDataReceiver.DataResource.class) {
                dataResClazz = c;
                break;
            }
        }
        Constructor<?> ctor =
                dataResClazz.getDeclaredConstructor(CoapDataReceiver.class, String.class, String.class);
        ctor.setAccessible(true);
        Object dataResource = ctor.newInstance(receiver, gatewayId, topic);
        Method handlePOST = dataResClazz.getMethod("handlePOST", CoapExchange.class);

        // 잘못된 포맷
        CoapExchange exchange = mock(CoapExchange.class);
        when(exchange.getRequestText()).thenReturn("not,a,valid,payload");

        handlePOST.invoke(dataResource, exchange);

        // 서비스 호출 없어야 함
        verify(gatewayConnector, never()).receiveGatewayData(anyString(), any(DataRequest.class));
        // BAD_REQUEST 응답
        verify(exchange).respond(CoAP.ResponseCode.BAD_REQUEST, "Invalid payload");
    }
}
