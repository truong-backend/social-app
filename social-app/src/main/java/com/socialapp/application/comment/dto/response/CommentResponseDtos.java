package com.socialapp.application.comment.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResponseDtos {

    public record CommentResponse(
            String id,
            String authorId,
            String authorUsername,
            String authorProfilePic,
            String postId,
            String repliedToCommentId,
            String content,
            List<String> attachedFileUrls,
            int likeCount,
            int replyCount,
            boolean isLiked,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record MessageResponse(String message) {}
}