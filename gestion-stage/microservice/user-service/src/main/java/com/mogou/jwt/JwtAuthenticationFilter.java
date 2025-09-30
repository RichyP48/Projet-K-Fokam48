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

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        // --- CORRECTION APPLIQUÉE ICI ---
        userEmail = jwtUtil.getUsernameFromToken(jwt); // Utilisation du bon nom de méthode

        // Si on a un email et que l'utilisateur n'est pas déjà authentifié
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                // Il est plus robuste de recharger les détails de l'utilisateur pour vérifier qu'il est toujours actif
                var userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                
                System.out.println("🔍 JWT Filter - User: " + userEmail);
                System.out.println("🔍 JWT Filter - Authorities: " + userDetails.getAuthorities());

                if (jwtUtil.validateToken(jwt)) { // Valide le token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Mettre à jour le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ JWT Filter - Authentication set for: " + userEmail);
                } else {
                    System.out.println("❌ JWT Filter - Invalid token for: " + userEmail);
                }
            } catch (Exception e) {
                System.out.println("❌ JWT Filter - Error loading user: " + e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
