package com.socialapp.infrastructure.security.jwt;

import com.socialapp.application.shared.port.TokenProvider;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider implements TokenProvider {

    private final SecretKey  secretKey;
    private final long       accessExpiryMs;
    private final long       refreshExpiryMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expiry-ms:3600000}") long accessExpiryMs,
            @Value("${app.jwt.refresh-expiry-ms:604800000}") long refreshExpiryMs) {
        this.secretKey       = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiryMs  = accessExpiryMs;
        this.refreshExpiryMs = refreshExpiryMs;
    }

    @Override
    public String generateAccessToken(String accountId, String role) {
        return Jwts.builder()
                .subject(accountId)
                .claim("role", role)
                .claim("type", "ACCESS")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiryMs))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String generateRefreshToken(String accountId) {
        return Jwts.builder()
                .subject(accountId)
                .claim("type", "REFRESH")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiryMs))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String extractAccountId(String token) {
        return parseClaims(token).getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    public String extractRole(String token) {
        return (String) parseClaims(token).get("role");
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
