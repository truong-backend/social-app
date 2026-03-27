package com.socialapp.application.shared.port;

/**
 * Outbound Port: TokenProvider
 * Được implement ở infrastructure (JwtTokenProvider)
 */
public interface TokenProvider {
    String generateAccessToken(String accountId, String role);
    String generateRefreshToken(String accountId);
    String extractAccountId(String token);
    boolean validateToken(String token);
}