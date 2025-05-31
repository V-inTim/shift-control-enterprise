package com.example.shift_control_enterprise.security;

import com.example.shift_control_enterprise.exception.EnterpriseException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthUtils {
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("Unauthorized or invalid JWT token");
        }
        return  UUID.fromString(jwt.getClaim("user_id"));
    }
    public Long getEnterpriseId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("Неавторизованный и инвалидный JWT token");
        }
        try {
            String enterpriseIdClaim = jwt.getClaim("enterprise_id").toString();
            return Long.valueOf(enterpriseIdClaim);
        } catch (NumberFormatException e) {
            throw new EnterpriseException("Инвалидный enterprise_id в JWT token");
        }
    }
}
