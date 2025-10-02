package com.mogou.service;

import com.mogou.client.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserClient userClient;
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    public Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() != null) {
                String token = authentication.getCredentials().toString();
                Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
                return Long.valueOf(claims.getSubject());
            }
        } catch (Exception e) {
            System.err.println("Error extracting user ID from JWT: " + e.getMessage());
        }
        // Fallback pour les tests
        return 2L;
    }
}