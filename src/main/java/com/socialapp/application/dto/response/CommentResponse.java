package com.socialapp.application.dto.response;

import java.time.LocalDateTime;

public record CommentResponse(
        String        id,
        String        authorId,
        String        authorName,
        String        authorAvatarUrl,
        String        content,
        int           likeCount,
        int           replyCount,
        String        attachmentUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}