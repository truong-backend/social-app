package com.socialapp.application.dto.response;

import java.time.LocalDate;

public record UserResponse(
        String    id,
        String    username,
        String    familyName,
        String    givenName,
        String    bio,
        LocalDate birthdate,
        int       friendCount,
        int       requestSentCount,
        int       requestReceivedCount,
        int       blockCount,
        String    profilePictureUrl,
        LocalDate nextChangeNameDate,
        LocalDate nextChangeBirthdateDate,
        LocalDate nextChangeUsernameDate
) {}