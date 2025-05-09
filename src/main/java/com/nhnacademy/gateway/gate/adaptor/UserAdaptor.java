package com.nhnacademy.gateway.gate.adaptor;

import com.nhnacademy.gateway.gate.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", path="/users")
public interface UserAdaptor {

    @GetMapping("/me")
    ResponseEntity<UserResponse> getUserInfo(@RequestHeader("X-User-Id") String encryptedEmail);

}
