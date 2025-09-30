package com.mogou.config;

import com.mogou.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // On désactive CSRF car on utilise des tokens JWT (stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // On n'utilise pas de session côté serveur
                .authorizeHttpRequests(auth -> auth
                        // On autorise la connexion initiale au WebSocket sans authentification HTTP
                        .requestMatchers("/ws/notifications/**").permitAll()
                        // On exige une authentification pour toutes les autres requêtes API
                        .requestMatchers("/api/**").authenticated()
                        // Toute autre requête doit être authentifiée
                        .anyRequest().authenticated()
                )
                // On ajoute notre filtre JWT avant le filtre de base de Spring Security
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
