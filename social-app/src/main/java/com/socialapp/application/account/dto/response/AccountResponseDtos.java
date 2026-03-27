package com.socialapp.application.account.dto.response;


public class AccountResponseDtos {

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String accountId,
            String userId,
            String role
    ) {}

    public record RegisterResponse(
            String accountId,
            String email,
            String message    // "Verification email sent"
    ) {}

    public record MessageResponse(
            String message
    ) {}
}