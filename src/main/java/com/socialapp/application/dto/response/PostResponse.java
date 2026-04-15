package com.socialapp.application.dto.response;

import com.socialapp.domain.model.valueobject.PostPrivacy;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        String        id,
        String        authorId,
        String        authorName,
        String        authorAvatarUrl,
        String        content,
        PostPrivacy   privacy,
        int           likeCount,
        int           shareCount,
        int           commentCount,
        List<String>  attachmentUrls,
        String        sharedFromPostId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentResponse> comments   // ← THÊM
) {}