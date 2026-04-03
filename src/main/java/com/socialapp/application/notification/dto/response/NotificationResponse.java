package com.socialapp.application.notification.dto.response;

import java.time.LocalDateTime;

public record NotificationResponse(
        String id,
        String byUserId,
        String action,
        String targetType,
        String targetId,
        boolean isRead,
        LocalDateTime sentAt
) {
}
