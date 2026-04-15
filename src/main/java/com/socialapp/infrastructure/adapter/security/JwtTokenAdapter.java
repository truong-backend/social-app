package com.socialapp.infrastructure.adapter.security;

import com.socialapp.application.port.TokenPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenAdapter implements TokenPort {

    private static final String CLAIM_ROLE = "role";
    private static final long   EXPIRES_IN = 86_400L; // 24 giờ (seconds)

    private final Key key;

    public JwtTokenAdapter(@Value("${app.jwt.secret}") String secret) {
        // secret phải >= 256 bits (32 bytes) cho HS256
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public String generate(String userId, String role) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + EXPIRES_IN * 1000);

        return Jwts.builder()
                .setSubject(userId)
                .claim(CLAIM_ROLE, role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public long expiresInSeconds() {
        return EXPIRES_IN;
    }

    @Override
    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    @Override
    public String extractRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    @Override
    public boolean isValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ── Helper ───────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}