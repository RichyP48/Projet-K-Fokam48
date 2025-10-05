package com.mogou.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userEmail = jwtUtil.getUsernameFromToken(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt)) {
                String role = jwtUtil.getRoleFromToken(jwt);
                
                // Normalise le rôle (supprime "ROLE_" si présent)
                String authority = role.startsWith("ROLE_") ? role.substring(5) : role;
                
                // Ajoute l'autorité normalisée
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));
                
                System.out.println("[JWT-FILTER] User: " + userEmail + ", Role: " + authority + ", Path: " + path);
                System.out.println("[JWT-FILTER] Authorities: " + authorities);
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEmail,  // principal = email
                        jwt,        // credentials = token JWT
                        authorities
                );
                authToken.setDetails(userEmail);
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                System.out.println("[JWT-FILTER] Authentication set successfully");
            } else {
                System.out.println("[JWT-FILTER] Token validation failed for user: " + userEmail);
            }
        }
        filterChain.doFilter(request, response);
    }
}