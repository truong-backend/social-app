package com.socialapp.application.dto.response;

import com.socialapp.domain.model.valueobject.UserRole;

public record AccountResponse(
        String   id,
        String   email,
        UserRole role,
        boolean  isVerified
) {}