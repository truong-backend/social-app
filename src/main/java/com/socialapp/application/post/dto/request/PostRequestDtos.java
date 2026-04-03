package com.socialapp.application.post.dto.request;

import jakarta.validation.constraints.NotNull;

public class PostRequestDtos {

    public record CreatePostRequest(
            String content,
            @NotNull String privacy   // "PUBLIC" | "FRIENDS" | "PRIVATE"
    ) {}

    public record SharePostRequest(
            String content,
            @NotNull String privacy
    ) {}

    public record UpdatePostContentRequest(
            String content
    ) {}

    public record UpdatePostPrivacyRequest(
            @NotNull String privacy
    ) {}
}