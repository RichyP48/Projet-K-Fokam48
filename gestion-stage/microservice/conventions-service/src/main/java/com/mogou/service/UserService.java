package com.mogou.service;

import com.mogou.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtUtil jwtUtil;

    public Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof String) {
                String token = (String) authentication.getPrincipal();
                return jwtUtil.getClaimFromToken(token, claims -> claims.get("userId", Long.class));
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public String getCurrentUserRole() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof String) {
                String token = (String) authentication.getPrincipal();
                return jwtUtil.getRoleFromToken(token);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}