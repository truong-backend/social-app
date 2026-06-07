package com.stu.socialnetworkapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GroupTextMessageRequest(
        @NotBlank(message = "TEXT_MESSAGE_CONTENT_REQUIRED")
        String text
) {
}