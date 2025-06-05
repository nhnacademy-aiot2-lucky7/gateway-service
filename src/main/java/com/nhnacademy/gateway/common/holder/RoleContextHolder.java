package com.nhnacademy.gateway.common.holder;

public final class RoleContextHolder {

    private static final ThreadLocal<String> ROLE_HOLDER = new ThreadLocal<>();

    private RoleContextHolder() {
        throw new IllegalStateException("Context Holder class");
    }

    public static String getRole() {
        return ROLE_HOLDER.get();
    }

    public static void setRole(String role) {
        ROLE_HOLDER.set(role);
    }

    public static void clear() {
        ROLE_HOLDER.remove();
    }
}
