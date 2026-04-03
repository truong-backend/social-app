package com.socialapp.application.post.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponseDtos {

    public record PostResponse(
            String id,
            String authorId,
            String authorUsername,
            String authorProfilePic,
            String content,
            String privacy,
            int likeCount,
            int shareCount,
            int commentCount,
            boolean isLiked,
            boolean isShared,
            String sharedFromPostId,
            List<String> attachedFileUrls,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record PostSummaryResponse(
            String id,
            String authorId,
            String content,
            String privacy,
            int likeCount,
            int commentCount,
            LocalDateTime createdAt
    ) {}

    public record MessageResponse(String message) {}
}
