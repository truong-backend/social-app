package com.socialapp.application.dto.response;

import java.time.LocalDateTime;

public record MessageResponse(
        String        id,
        String        senderId,
        String        senderName,
        String        content,
        boolean       isRead,
        boolean       isDeleted,
        String        attachmentUrl,
        LocalDateTime sentAt,
        LocalDateTime updatedAt
) {}