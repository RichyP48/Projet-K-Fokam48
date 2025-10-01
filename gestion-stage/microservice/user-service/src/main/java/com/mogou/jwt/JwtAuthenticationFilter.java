package com.mogou.jwt;

import com.mogou.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService; // Il est préférable d'injecter le service

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Ignorer les endpoints publics
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/") || path.startsWith("/swagger-ui/") || 
            path.startsWith("/v3/api-docs/") || path.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Vérifier d'abord les headers du Gateway
        String userEmail = request.getHeader("X-User-Username");
        String userRoles = request.getHeader("X-User-Roles");
        
        if (userEmail != null) {
            // Authentification via headers du Gateway
            try {
                var userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                System.out.println("❌ Gateway Auth - Error loading user: " + e.getMessage());
            }
            filterChain.doFilter(request, response);
            return;
        }

        // Fallback vers Authorization header pour tests directs
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        System.out.println("🔍 JWT Token received: " + jwt.substring(0, Math.min(50, jwt.length())) + "...");
        
        try {
            userEmail = jwtUtil.getUsernameFromToken(jwt);
            System.out.println("🔍 JWT Filter - Extracted email: " + userEmail);
        } catch (Exception e) {
            System.out.println("❌ JWT Filter - Error extracting email: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                var userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                System.out.println("🔍 JWT Filter - User loaded: " + userEmail);
                System.out.println("🔍 JWT Filter - Authorities: " + userDetails.getAuthorities());

                if (jwtUtil.validateToken(jwt)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ JWT Filter - Authentication set for: " + userEmail);
                } else {
                    System.out.println("❌ JWT Filter - Token validation failed for: " + userEmail);
                }
            } catch (Exception e) {
                System.out.println("❌ JWT Filter - Error loading user: " + e.getMessage());
                e.printStackTrace();
            }
        }
        filterChain.doFilter(request, response);
    }
}
