package com.nhnacademy.gateway.infrastructure.adapter;

import com.nhnacademy.gateway.infrastructure.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserServiceAdapter {

    @GetMapping("/users/me")
    UserResponse getUser(
            @RequestHeader("X-USER-ID") String encryptedEmail
    );
}
