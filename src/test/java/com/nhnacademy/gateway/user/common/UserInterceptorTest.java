package com.nhnacademy.gateway.user.common;

import com.nhnacademy.gateway.exception.MissingHeaderException;
import com.nhnacademy.gateway.exception.UserNotFoundException;
import com.nhnacademy.gateway.user.adaptor.UserAdaptor;
import com.nhnacademy.gateway.user.dto.DepartmentResponse;
import com.nhnacademy.gateway.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserInterceptorTest {

    @InjectMocks
    UserInterceptor userInterceptor;

    @Mock
    UserAdaptor userAdaptor;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    Object handler;

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void preHandle_successfully_sets_departmentId() {
        // given
        when(request.getHeader("X-User-Id")).thenReturn("user-01");

        DepartmentResponse department = new DepartmentResponse("dept-01", "개발부");
        UserResponse userResponse = new UserResponse("ROLE_USER", 1L, "홍길동", "hong@example.com", "010-1234-5678", department, null);
        when(userAdaptor.getUserInfo("user-01")).thenReturn(ResponseEntity.ok(userResponse));

        // when
        boolean result = userInterceptor.preHandle(request, response, handler);

        // then
        assertTrue(result);
        assertEquals("dept-01", UserContextHolder.getDepartmentId());
    }

    @Test
    void preHandle_missingHeader_throwsException() {
        when(request.getHeader("X-User-Id")).thenReturn(null);

        assertThrows(MissingHeaderException.class, () ->
                userInterceptor.preHandle(request, response, handler));
    }

    @Test
    void preHandle_userNotFound_throwsException() {
        when(request.getHeader("X-User-Id")).thenReturn("user-01");
        when(userAdaptor.getUserInfo("user-01")).thenReturn(ResponseEntity.ok(null));

        assertThrows(UserNotFoundException.class, () ->
                userInterceptor.preHandle(request, response, handler));
    }

    @Test
    void afterCompletion_clears_context() {
        // given
        UserContextHolder.setDepartmentId("dept-01");
        assertEquals("dept-01", UserContextHolder.getDepartmentId());

        // when
        userInterceptor.afterCompletion(request, response, handler, null);

        // then
        assertNull(UserContextHolder.getDepartmentId());
    }
}
