package com.dorm.backend.common;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;
    private final SecretKey secretKey;

    public JwtUtils(@Value("${jwt.secret:dormitory-default-secret-key-change-in-prod!!!}") String configuredSecret) {
        String secret = configuredSecret != null && !configuredSecret.isBlank()
                ? configuredSecret
                : "dormitory-default-secret-key-change-in-prod!!!";
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    public Map<String, Object> parseToken(String token) {
        var claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Map<String, Object> result = new HashMap<>();
        result.put("userId", ((Number) claims.get("userId")).longValue());
        result.put("username", claims.getSubject());
        result.put("role", claims.get("role", String.class));
        return result;
    }
}
