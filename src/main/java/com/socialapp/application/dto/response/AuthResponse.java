package com.socialapp.application.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long   expiresIn,       // seconds
        String accountId,
        String userId
) {}