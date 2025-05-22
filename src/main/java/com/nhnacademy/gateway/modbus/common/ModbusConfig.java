package com.nhnacademy.gateway.modbus.common;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.ip.IpParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModbusConfig {

    @Bean
    public ModbusMaster modbusMaster() throws ModbusInitException {
        IpParameters params = new IpParameters();
        params.setHost("192.168.70.203");
        params.setPort(502);

        ModbusFactory factory = new ModbusFactory();
        ModbusMaster master = factory.createTcpMaster(params, true);
        master.init();

        return master;
    }
}
