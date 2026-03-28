package com.socialapp.application.user.dto.response;

import java.time.LocalDate;

public class UserResponseDtos {

    public record UserProfileResponse(
            String id,
            String username,
            String familyName,
            String givenName,
            String bio,
            String profilePictureUrl,
            LocalDate birthdate,
            int friendCount,
            boolean isFriend,
            boolean isBlocked,
            boolean hasSentRequest,
            boolean hasReceivedRequest
    ) {}

    public record UserSummaryResponse(
            String id,
            String username,
            String familyName,
            String givenName,
            String profilePictureUrl
    ) {}

    public record MessageResponse(String message) {}
}
