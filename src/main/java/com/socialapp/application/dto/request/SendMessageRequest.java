package com.socialapp.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(

        @NotBlank
        String chatId,

        @Size(max = 10_000)
        String content          // null nếu chỉ gửi file
) {}