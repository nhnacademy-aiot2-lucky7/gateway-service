package com.nhnacademy.gateway.mqtt.receivedata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DataRequest {

    @JsonProperty("topic")
    @NotBlank(message = "토픽은 필수 입력 항목입니다.") // 빈 문자열 방지
    private String topic;

    @JsonProperty("time")
    @NotNull(message = "시간은 필수 입력 항목입니다.")
    @Positive(message = "시간은 0보다 큰 값이어야 합니다.") // 시간 값 검증
    private Long time;

    @JsonProperty("value")
    @NotNull(message = "값은 필수 입력 항목입니다.")
    @Positive(message = "값은 0보다 큰 값이어야 합니다.") // 값 검증
    private Double value;
}