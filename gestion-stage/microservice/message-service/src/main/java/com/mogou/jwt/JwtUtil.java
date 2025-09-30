package com.mogou.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Valide le token en vérifiant le nom d'utilisateur et la date d'expiration.
     * @param token Le token JWT.
     * @return true si le token est valide, false sinon.
     */
    public Boolean validateToken(String token) {
        try {
            // Jwts.parserBuilder()...parseClaimsJws(token) lèvera une exception si le token est invalide (signature, format, etc.)
            // Nous vérifions simplement qu'il n'est pas expiré.
            return !isTokenExpired(token);
        } catch (Exception e) {
            // Le token est invalide pour une raison quelconque (mauvaise signature, malformé, etc.)
            return false;
        }
    }
}
