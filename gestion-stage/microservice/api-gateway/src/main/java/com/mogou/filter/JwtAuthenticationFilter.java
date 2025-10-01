package com.mogou.filter;

import com.mogou.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        System.out.println("🔍 Gateway Filter - Processing request: " + request.getURI().getPath());

        final List<String> publicEndpoints = List.of("/api/auth/register", "/api/auth/login", "/api/academic");
        final List<String> publicGetEndpoints = List.of("/api/offers");
        
        // Allow GET requests to offers for browsing
        if (publicGetEndpoints.stream().anyMatch(uri -> request.getURI().getPath().contains(uri)) && 
            "GET".equals(request.getMethod().name())) {
            System.out.println("🔓 Gateway - Public GET endpoint: " + request.getURI().getPath());
            return chain.filter(exchange);
        }

        if (publicEndpoints.stream().anyMatch(uri -> request.getURI().getPath().contains(uri))) {
            System.out.println("🔓 Gateway - Public endpoint, skipping auth: " + request.getURI().getPath());
            return chain.filter(exchange);
        }

        final String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return this.onError(exchange, "Aucun token d'autorisation trouvé.", HttpStatus.UNAUTHORIZED);
        }

        final String token = authHeader.substring(7);
        System.out.println("🔍 Gateway - Token received: " + token.substring(0, Math.min(50, token.length())) + "...");
        System.out.println("🔍 Gateway - Path: " + request.getURI().getPath());

        try {
            if (!jwtUtil.validateToken(token)) {
                System.out.println("❌ Gateway - Token validation failed");
                return this.onError(exchange, "Le token est invalide ou a expiré.", HttpStatus.UNAUTHORIZED);
            }
            System.out.println("✅ Gateway - Token validated successfully");
        } catch (Exception e) {
            System.out.println("❌ Gateway - Token validation error: " + e.getMessage());
            return this.onError(exchange, "Le token est invalide ou a expiré.", HttpStatus.UNAUTHORIZED);
        }

        ServerHttpRequest modifiedRequest = addClaimsToHeader(request, token);
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private ServerHttpRequest addClaimsToHeader(ServerHttpRequest request, String token) {
        try {
            Claims claims = jwtUtil.getClaimsFromToken(token);
            String username = claims.getSubject();
            String role = String.valueOf(claims.get("role"));
            String userId = String.valueOf(claims.get("userId"));
            System.out.println("🔍 Gateway - Adding headers: Username=" + username + ", Role=" + role + ", UserId=" + userId);
            return request.mutate()
                    .header("X-User-Username", username)
                    .header("X-User-Roles", role)
                    .header("X-User-Id", userId)
                    .build();
        } catch (Exception e) {
            System.out.println("❌ Gateway - Error adding headers: " + e.getMessage());
            return request;
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
