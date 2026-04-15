package com.socialapp.application.dto.response;

import java.time.LocalDateTime;

public record NotificationResponse(
        String        id,
        String        action,
        String        targetType,
        String        targetId,
        boolean       isRead,
        LocalDateTime sentAt
) {}