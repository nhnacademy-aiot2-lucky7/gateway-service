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

    // 채널 설정
    private final Map<String, ChannelInfo> channelMap = Map.of(
            "location1", new ChannelInfo(1, 100),
            "location2", new ChannelInfo(1, 200),
            "location3", new ChannelInfo(1, 300)
    );

    // 전력/전류/전압 필드 정의 (공통)
    private static final List<ModbusField> COMMON_FIELDS = List.of(
            new ModbusField("power", 4, 5, 0.01),
            new ModbusField("current", 2, 3, 0.01),
            new ModbusField("voltage", 16, 17, 0.1)
    );

    public List<ModbusResult> collectAllChannelData() {
        List<ModbusResult> results = new ArrayList<>();

        for (Map.Entry<String, ChannelInfo> entry : channelMap.entrySet()) {
            String location = entry.getKey();
            ChannelInfo info = entry.getValue();

            try {
                Map<String, Double> values = new HashMap<>();
                for (ModbusField field : COMMON_FIELDS) {
                    double value = readField(info, field);
                    values.put(field.name(), value);
                }

                results.add(new ModbusResult(
                        location,
                        values.get("current"),
                        values.get("voltage"),
                        values.get("power"),
                        Instant.now().toEpochMilli()
                ));

            } catch (Exception e) {
                log.error("Modbus 읽기 실패 - 채널: {}, slaveId: {}, baseAddress: {}", location, info.slaveId, info.baseAddress, e);
                throw new ModbusReadException();
            }
        }

        return results;
    }

    private double readField(ChannelInfo info, ModbusField field) throws Exception {
        int high = modbusMaster.getValue(BaseLocator.holdingRegister(
                info.slaveId,
                info.baseAddress + field.highOffset(),
                DataType.TWO_BYTE_INT_UNSIGNED)).intValue();

        int low = modbusMaster.getValue(BaseLocator.holdingRegister(
                info.slaveId,
                info.baseAddress + field.lowOffset(),
                DataType.TWO_BYTE_INT_UNSIGNED)).intValue();

        long raw = ((long) high << 16) | (low & 0xFFFFL);
        return raw * field.scale();
    }

    private static class ChannelInfo {
        int slaveId;
        int baseAddress;

        ChannelInfo(int slaveId, int baseAddress) {
            this.slaveId = slaveId;
            this.baseAddress = baseAddress;
        }
    }

    private static class ModbusField {
        private final String name;
        private final int highOffset;
        private final int lowOffset;
        private final double scale;

        public ModbusField(String name, int highOffset, int lowOffset, double scale) {
            this.name = name;
            this.highOffset = highOffset;
            this.lowOffset = lowOffset;
            this.scale = scale;
        }

        public String name() {
            return name;
        }

        public int highOffset() {
            return highOffset;
        }

        public int lowOffset() {
            return lowOffset;
        }

        public double scale() {
            return scale;
        }
    }
}
