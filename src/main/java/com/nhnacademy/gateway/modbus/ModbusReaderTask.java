package com.nhnacademy.gateway.modbus;

import com.nhnacademy.gateway.exception.ModbusReadException;
import com.nhnacademy.gateway.exception.MqttPublishException;
import com.nhnacademy.gateway.modbus.ModbusDataCollector;
import com.nhnacademy.gateway.modbus.dto.ModbusResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ModbusReaderTask {

    private final ModbusDataCollector collector;
    private final ModbusMqttPublisher publisher;

    @Scheduled(fixedRate = 60_000) // 1분마다 실행
    public void readAndPublish() {
        try {
            List<ModbusResult> results = collector.collectAllChannelData();
            results.forEach(result -> log.info("Collected: {}", result));

            publisher.publish(results);

        } catch (ModbusReadException e) {
            log.error("Modbus 데이터 읽기 오류 - 상태코드: {}, 메시지: {}", e.getStatusCode(), e.getMessage(), e);
            // 필요하다면 재시도 로직, 알림, fallback 처리 등 추가 가능

        } catch (MqttPublishException e) {
            log.error("MQTT 발행 오류 - 상태코드: {}, 메시지: {}", e.getStatusCode(), e.getMessage(), e);
            // 마찬가지로 복구 조치 또는 알림 처리

        } catch (Exception e) {
            log.error("ModbusReaderTask 처리 중 알 수 없는 오류 발생", e);
            // 예외 종류별 구분이 안 되거나, 예상치 못한 예외에 대한 최후의 catch
        }
    }
}
