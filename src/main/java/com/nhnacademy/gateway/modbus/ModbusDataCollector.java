package com.nhnacademy.gateway.modbus;

import com.nhnacademy.gateway.exception.ModbusReadException;
import com.nhnacademy.gateway.modbus.dto.ModbusResult;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.locator.BaseLocator;
import com.serotonin.modbus4j.code.DataType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ModbusDataCollector {

    private final ModbusMaster modbusMaster;

    // 채널 설정: 예시 (실제 환경에 맞게)
    private final Map<String, ChannelInfo> channelMap = Map.of(
            "location1", new ChannelInfo(1, 600),
            "location2", new ChannelInfo(1, 700),
            "location3", new ChannelInfo(2, 600)
    );

    public List<ModbusResult> collectAllChannelData() {
        List<ModbusResult> results = new ArrayList<>();

        for (Map.Entry<String, ChannelInfo> entry : channelMap.entrySet()) {
            String channel = entry.getKey();
            ChannelInfo info = entry.getValue();
            try {
                double current = readRegister(info, 2, 3);
                double voltage = readRegister(info, 16, 17);
                double power = readRegister(info, 4, 5);

                results.add(new ModbusResult(channel, current, voltage, power, Instant.now().toEpochMilli()));

            } catch (Exception e) {
                String errMsg = String.format("Modbus 읽기 실패 - 채널: %s, slaveId: %d, baseAddress: %d", channel, info.slaveId, info.baseAddress);
                log.error(errMsg, e);
                throw new ModbusReadException();
            }
        }

        return results;
    }

    private double readRegister(ChannelInfo info, int highOffset, int lowOffset) throws Exception {
        int high = modbusMaster.getValue(BaseLocator.holdingRegister(info.slaveId, info.baseAddress + highOffset, DataType.TWO_BYTE_INT_UNSIGNED)).intValue();
        int low = modbusMaster.getValue(BaseLocator.holdingRegister(info.slaveId, info.baseAddress + lowOffset, DataType.TWO_BYTE_INT_UNSIGNED)).intValue();
        long raw = ((long) high << 16) | low;
        return raw / 100.0;
    }

    private static class ChannelInfo {
        int slaveId;
        int baseAddress;

        ChannelInfo(int slaveId, int baseAddress) {
            this.slaveId = slaveId;
            this.baseAddress = baseAddress;
        }
    }
}
