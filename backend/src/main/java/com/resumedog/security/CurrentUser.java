package com.resumedog.security;

import com.resumedog.common.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 从 SecurityContext 里取当前登录用户 ID。
 * JwtAuthenticationFilter 放入的 principal 就是 userId。
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new BusinessException(40100, "未登录");
        }
        return userId;
    }
}
