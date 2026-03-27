package com.socialapp.application.message.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class MessageResponseDtos {

    public record ChatResponse(
            String id,
            List<String> memberIds,
            LocalDateTime createdAt
    ) {}

    public record MessageResponse(
            String id,
            String senderId,
            String chatId,
            String content,
            List<String> attachedFileUrls,
            boolean isRead,
            LocalDateTime sentAt,
            LocalDateTime updatedAt
    ) {}

    public record SimpleMessageResponse(String message) {}
}