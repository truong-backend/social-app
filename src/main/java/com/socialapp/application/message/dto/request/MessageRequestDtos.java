package com.socialapp.application.message.dto.request;

import jakarta.validation.constraints.NotBlank;

public class MessageRequestDtos {
    public record SendMessageRequest(String content) {}
    public record UpdateMessageRequest(@NotBlank String content) {}
    public record DeleteMessageRequest(@NotBlank String type) {}  // "EVERY" | "USER_ONLY"
}