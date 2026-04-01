package com.hellFire.Real_Time_Notifications_System.util;

import com.hellFire.Real_Time_Notifications_System.models.AppUsers;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    public enum JwtValidationStatus {
        VALID,
        EXPIRED,
        INVALID
    }

    private final SecretKey key;

    public JwtUtil(@Value("${app.jwt.secret}") String jwtSecret) {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("Missing required environment variable: JWT_SECRET");
        }

        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(AppUsers user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("userRole", user.getUserRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Distinguishes expired vs other invalid tokens so HTTP filters can return {@code TOKEN_EXPIRED}
     * for the client session-refresh flow.
     */
    public JwtValidationStatus classifyToken(String token) {
        if (token == null || token.isBlank()) {
            return JwtValidationStatus.INVALID;
        }
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return JwtValidationStatus.VALID;
        } catch (ExpiredJwtException e) {
            return JwtValidationStatus.EXPIRED;
        } catch (JwtException | IllegalArgumentException e) {
            return JwtValidationStatus.INVALID;
        }
    }

    public boolean isTokenValid(String token) {
        return classifyToken(token) == JwtValidationStatus.VALID;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}