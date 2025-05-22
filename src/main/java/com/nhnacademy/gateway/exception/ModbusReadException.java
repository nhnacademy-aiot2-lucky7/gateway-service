package com.nhnacademy.gateway.exception;

public class ModbusReadException extends CommonHttpException {

    private static final int HTTP_STATUS_CODE = 500;

    public ModbusReadException() {
        super(HTTP_STATUS_CODE, "Modbus 데이터 읽기 중 오류가 발생했습니다.");
    }

    public ModbusReadException(String message) {
        super(HTTP_STATUS_CODE, message);
    }

}