package com.nhnacademy.gateway.user.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserResponse {

    String userRole;

    Long userNo;

    String userName;

    String userEmail;

    String userPhone;

    String userDepartment;
}
