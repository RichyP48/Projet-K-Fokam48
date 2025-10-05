package com.mogou.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

     @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // Récupère la requête HTTP entrante actuelle
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    
                    // Récupère le header "Authorization" complet de la requête initiale
                    String authorizationHeader = request.getHeader("Authorization");
                    
                    // Si le header existe, on le transmet à la requête sortante de Feign
                    if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
                        template.header("Authorization", authorizationHeader);
                    }
                }
            }
        };
    }
}