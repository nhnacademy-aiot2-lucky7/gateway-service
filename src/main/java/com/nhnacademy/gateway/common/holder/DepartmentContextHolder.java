package com.nhnacademy.gateway.common.holder;

public final class DepartmentContextHolder {

    private static final ThreadLocal<String> DEPARTMENT_HOLDER = new ThreadLocal<>();

    private DepartmentContextHolder() {
        throw new IllegalStateException("Context Holder class");
    }

    public static String getDepartmentId() {
        return DEPARTMENT_HOLDER.get();
    }

    public static void setDepartmentId(String departmentId) {
        DEPARTMENT_HOLDER.set(departmentId);
    }

    public static void clear() {
        DEPARTMENT_HOLDER.remove();
    }
}
