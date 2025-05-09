package com.nhnacademy.gateway.gate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GateRequest {

    @JsonProperty("gateName")
    @NotBlank(message = "게이트 이름은 필수 입력 항목입니다.")
    @Size(min = 2, max = 30, message = "게이트 이름은 2자 이상 30자 이하로 입력해주세요.")
    private String gateName;

    @JsonProperty("protocol")
    @NotBlank(message = "통신 방식은 필수 입력 항목입니다.")
    @Pattern(regexp = "^(HTTP|MQTT|CoAP)$", message = "통신 방식은 'HTTP', 'MQTT', 'CoAP' 중 하나여야 합니다.")
    private String protocol;

    @JsonProperty("brokerIp")
    @NotBlank(message = "Broker IP 주소는 필수 입력 항목입니다.")
    @Pattern(
            regexp = "^([a-zA-Z0-9.-]+|\\d{1,3}(\\.\\d{1,3}){3})$",
            message = "유효한 IP 주소 또는 도메인 이름을 입력해주세요."
    )
    private String brokerIp;

    @JsonProperty("port")
    @NotNull(message = "포트 번호는 필수 입력 항목입니다.")
    @Min(value = 1, message = "포트 번호는 1 이상이어야 합니다.")
    @Max(value = 65535, message = "포트 번호는 65535 이하이어야 합니다.")
    private Integer port;

    @JsonProperty("description")
    @Size(max = 100, message = "설명은 100자 이하로 입력해주세요.")
    private String description;
}