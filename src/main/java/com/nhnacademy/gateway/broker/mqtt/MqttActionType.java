package com.nhnacademy.gateway.broker.mqtt;

public enum MqttActionType {

    CONNECT("connect", "연결"),

    RECONNECT("reconnect", "재연결"),

    DISCONNECT("disconnect", "연결 해제"),

    SUBSCRIBE("subscribe", "구독"),

    UNSUBSCRIBE("unsubscribe", "구독 해제"),

    PUBLISH("publish", "발행"),

    UNKNOWN("unknown", "알 수 없음");

    private final String action;

    private final String department;

    MqttActionType(String action, String department) {
        this.action = action;
        this.department = department;
    }

    public String action() {
        return action;
    }

    public String department() {
        return department;
    }

    public boolean isConnectionAction() {
        return this == CONNECT || this == RECONNECT;
    }

    public static MqttActionType of(Object userContext) {
        if (userContext instanceof MqttActionType mqttActionType) {
            return mqttActionType;
        }
        return MqttActionType.UNKNOWN;
    }
}
