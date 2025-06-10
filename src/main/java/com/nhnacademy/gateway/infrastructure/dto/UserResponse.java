package com.nhnacademy.gateway.infrastructure.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public final class UserResponse {

    String userRole;

    Long userNo;

    String userName;

    String userEmail;

    String userPhone;

    DepartmentResponse department;

    EventLevelResponse eventLevelResponse;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class DepartmentResponse {

        private String departmentId;

        private String departmentName;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class EventLevelResponse {

        private String eventLevelName;

        private String eventLevelDetails;

        private Integer priority;
    }
}
