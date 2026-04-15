package com.socialapp.application.dto.request;

import com.socialapp.domain.model.valueobject.PostPrivacy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(

        @NotBlank
        @Size(max = 10_000)
        String content,

        @NotNull
        PostPrivacy privacy
) {}