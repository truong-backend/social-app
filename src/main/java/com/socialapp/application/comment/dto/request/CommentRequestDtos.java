package com.socialapp.application.comment.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CommentRequestDtos {
    public record CreateCommentRequest(@NotBlank String content) {}
    public record ReplyCommentRequest(@NotBlank String content) {}
    public record UpdateCommentRequest(@NotBlank String content) {}
}
