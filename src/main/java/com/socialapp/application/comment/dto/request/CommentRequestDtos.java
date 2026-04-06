package com.socialapp.application.comment.dto.request;

public class CommentRequestDtos {
    // content có thể null/blank khi bình luận chỉ bằng ảnh
    public record CreateCommentRequest(String content) {}
    public record ReplyCommentRequest(String content) {}
    public record UpdateCommentRequest(String content) {}
}