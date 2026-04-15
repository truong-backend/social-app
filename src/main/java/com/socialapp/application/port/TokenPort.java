package com.socialapp.application.port;

/**
 * Port (outbound) — Infrastructure sẽ implement bằng JWT.
 */
public interface TokenPort {
    String generate(String userId, String role);
    long   expiresInSeconds();
    String extractUserId(String token);
    String extractRole(String token);
    boolean isValid(String token);
}