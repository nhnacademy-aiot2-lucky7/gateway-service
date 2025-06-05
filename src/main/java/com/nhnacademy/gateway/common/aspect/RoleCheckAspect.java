package com.nhnacademy.gateway.common.aspect;

import com.nhnacademy.gateway.common.annotation.CheckRole;
import com.nhnacademy.gateway.common.exception.http.ForbiddenException;
import com.nhnacademy.gateway.common.holder.RoleContextHolder;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public final class RoleCheckAspect {

    @Before("@within(checkRole)")
    public void checkRoleOnClass(CheckRole checkRole) {
        verifyRole(checkRole.value().name());
    }

    @Before("@annotation(checkRole)")
    public void checkRoleOnMethod(CheckRole checkRole) {
        verifyRole(checkRole.value().name());
    }

    private void verifyRole(String required) {
        String actual = RoleContextHolder.getRole();
        if (!required.equals(actual)) {
            throw new ForbiddenException("권한 없음");
        }
    }
}
