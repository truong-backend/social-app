package com.socialapp.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ChatResponse(
        String             id,
        List<String>       memberIds,
        MessageResponse    lastMessage,
        LocalDateTime      createdAt
) {}