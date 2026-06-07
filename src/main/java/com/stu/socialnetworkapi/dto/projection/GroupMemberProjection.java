package com.stu.socialnetworkapi.dto.projection;

import java.time.ZonedDateTime;
import java.util.UUID;

public record GroupMemberProjection(
        UUID userId,
        String username,
        String givenName,
        String familyName,
        String profilePictureId,
        String role,
        ZonedDateTime joinedAt
) {}