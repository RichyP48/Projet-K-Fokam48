package com.mogou.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                // Essayer d'abord credentials (où le token JWT devrait être)
                if (auth.getCredentials() instanceof String) {
                    String token = (String) auth.getCredentials();
                    if (token != null && !token.isBlank()) {
                        requestTemplate.header("Authorization", "Bearer " + token);
                        System.out.println("[FEIGN] Propagating JWT token from credentials");
                        return;
                    }
                }
                // Fallback: essayer principal (pour compatibilité)
                if (auth.getPrincipal() instanceof String) {
                    String token = (String) auth.getPrincipal();
                    if (token != null && !token.isBlank() && token.contains(".")) {
                        requestTemplate.header("Authorization", "Bearer " + token);
                        System.out.println("[FEIGN] Propagating JWT token from principal");
                    }
                }
            }
        };
    }
}
