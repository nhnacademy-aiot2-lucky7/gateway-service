package com.nhnacademy.gateway.user.adaptor;

import com.nhnacademy.gateway.user.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", path="/users")
public interface UserAdaptor {

    @GetMapping("/me")
    ResponseEntity<UserResponse> getUserInfo(@RequestHeader("X-User-Id") String encryptedEmail);

}
