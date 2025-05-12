package com.nhnacademy.gateway.user.common;

import com.nhnacademy.gateway.user.adaptor.UserAdaptor;
import com.nhnacademy.gateway.user.dto.UserResponse;
import com.nhnacademy.gateway.gate.exception.MissingHeaderException;
import com.nhnacademy.gateway.gate.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserInterceptor implements HandlerInterceptor {

    private final UserAdaptor userAdaptor;

    public UserInterceptor(@Lazy UserAdaptor userAdaptor) {
        this.userAdaptor = userAdaptor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String encryptedEmail = request.getHeader("X-User-Id");

        if (encryptedEmail == null || encryptedEmail.isBlank()) {
            throw new MissingHeaderException("X-User-Id 헤더가 존재하지 않습니다.");
        }

        ResponseEntity<UserResponse> userInfo = userAdaptor.getUserInfo(encryptedEmail);

        if (userInfo == null || userInfo.getBody() == null) {
            throw new UserNotFoundException("유저 정보를 찾을 수 없습니다.");
        }

        String departmentId = userInfo.getBody().getUserDepartment();
        UserContextHolder.setDepartmentId(departmentId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear(); // 요청 끝나면 꼭 제거해야 함 (메모리 누수 방지)
    }
}
