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

        final List<String> publicEndpoints = List.of("/api/auth/register", "/api/auth/login");

        if (publicEndpoints.stream().anyMatch(uri -> request.getURI().getPath().contains(uri))) {
            return chain.filter(exchange);
        }

        final String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return this.onError(exchange, "Aucun token d'autorisation trouvé.", HttpStatus.UNAUTHORIZED);
        }

        final String token = authHeader.substring(7);

        try {
            if (!jwtUtil.validateToken(token)) {
                return this.onError(exchange, "Le token est invalide ou a expiré.", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
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
            return request.mutate()
                    .header("X-User-Username", claims.getSubject())
                    .header("X-User-Roles", String.valueOf(claims.get("roles")))
                    .build();
        } catch (Exception e) {
            return request;
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
