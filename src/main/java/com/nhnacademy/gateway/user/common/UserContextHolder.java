package com.nhnacademy.gateway.user.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

public class UserContextHolder {

    private UserContextHolder() {}

    private static final ThreadLocal<String> departmentId = new ThreadLocal<>();

    public static void setDepartmentId(String deptId) {
        departmentId.set(deptId);
    }

    public static String getDepartmentId() {
        return departmentId.get();
    }

    public static void clear() {
        departmentId.remove();
    }
}
