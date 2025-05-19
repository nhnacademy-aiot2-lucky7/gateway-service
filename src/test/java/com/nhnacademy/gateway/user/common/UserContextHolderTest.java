package com.nhnacademy.gateway.user.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserContextHolderTest {

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Test
    void test_set_and_get_departmentId() {
        UserContextHolder.setDepartmentId("dept-123");
        assertEquals("dept-123", UserContextHolder.getDepartmentId());
    }

    @Test
    void test_clear() {
        UserContextHolder.setDepartmentId("dept-456");
        UserContextHolder.clear();
        assertNull(UserContextHolder.getDepartmentId());
    }
}
