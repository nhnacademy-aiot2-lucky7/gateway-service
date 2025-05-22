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
    private final Map<String, ChannelInfo> channelMap = Map.ofEntries(
            Map.entry("server_room|장비1", new ChannelInfo(1, 100)),
            Map.entry("server_room|장비2", new ChannelInfo(1, 200)),
            Map.entry("class_a|장비1", new ChannelInfo(1, 300)),
            Map.entry("class_a|장비2", new ChannelInfo(1, 400)),
            Map.entry("class_b|장비1", new ChannelInfo(1, 500)),
            Map.entry("class_b|장비2", new ChannelInfo(1, 600)),
            Map.entry("office|장비1", new ChannelInfo(1, 700)),
            Map.entry("office|장비2", new ChannelInfo(1, 800)),
            Map.entry("meeting_room|장비1", new ChannelInfo(1, 900)),
            Map.entry("meeting_room|장비2", new ChannelInfo(1, 1000)),
            Map.entry("hive|장비1", new ChannelInfo(1, 1100)),
            Map.entry("hive|장비2", new ChannelInfo(1, 1200)),
            Map.entry("pair_room|장비1", new ChannelInfo(1, 1300)),
            Map.entry("pair_room|장비2", new ChannelInfo(1, 1400))
//            Map.entry("사무실_전열1", new ChannelInfo(1, 1500)),
//            Map.entry("사무실_전열2", new ChannelInfo(1, 1600)),
//            Map.entry("사무실_복사기", new ChannelInfo(1, 1700)),
//            Map.entry("빌트인_전열", new ChannelInfo(1, 1800)),
//            Map.entry("사무실_전열3", new ChannelInfo(1, 1900)),
//            Map.entry("정수기", new ChannelInfo(1, 2000)),
//            Map.entry("하이브_전열", new ChannelInfo(1, 2100)),
//            Map.entry("바텐_전열", new ChannelInfo(1, 2200)),
//            Map.entry("S_P", new ChannelInfo(1, 2300)),
//            Map.entry("공조기", new ChannelInfo(1, 2400))
    );

    // 전력/전류/전압 필드 정의 (공통)
    private static final List<ModbusField> COMMON_FIELDS = List.of(
            new ModbusField("power", 4, 5, 1),
            new ModbusField("current", 2, 3, 0.01),
            new ModbusField("voltage", 16, 17, 0.01)
    );

    public List<ModbusResult> collectAllChannelData() {
        List<ModbusResult> results = new ArrayList<>();

        for (Map.Entry<String, ChannelInfo> entry : channelMap.entrySet()) {
            String key = entry.getKey();  // e.g., "office|장비1"
            ChannelInfo info = entry.getValue();

            // 파싱
            String[] tokens = key.split("\\|", 2);
            String location = tokens[0];            // 공간
            String deviceName = tokens.length > 1 ? tokens[1] : "unknown";  // 위치

            try {
                Map<String, Double> values = new HashMap<>();
                for (ModbusField field : COMMON_FIELDS) {
                    double value = readField(info, field);
                    values.put(field.name(), value);
                }

                results.add(new ModbusResult(
                        location,
                        deviceName,
                        values.get("current"),
                        values.get("voltage"),
                        values.get("power"),
                        Instant.now().toEpochMilli()
                ));

            } catch (Exception e) {
                log.error("Modbus 읽기 실패 - 채널: {}, slaveId: {}, baseAddress: {}", key, info.slaveId, info.baseAddress, e);
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
