package com.nhnacademy.gateway.common.interceptor;

import com.nhnacademy.gateway.common.holder.RoleContextHolder;
import com.nhnacademy.gateway.infrastructure.adapter.UserServiceAdapter;
import com.nhnacademy.gateway.infrastructure.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class ContextInterceptor implements HandlerInterceptor {

    private final UserServiceAdapter userServiceAdapter;

    public ContextInterceptor(
            @Lazy UserServiceAdapter userServiceAdapter
    ) {
        this.userServiceAdapter = userServiceAdapter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String encryptedEmail = request.getHeader("X-USER-ID");

        if (encryptedEmail != null && !encryptedEmail.isBlank()) {
            UserResponse userResponse =
                    userServiceAdapter.getUser(encryptedEmail);

            RoleContextHolder.setRole(
                    userResponse.getUserRole()
            );
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        RoleContextHolder.clear();
    }
}
