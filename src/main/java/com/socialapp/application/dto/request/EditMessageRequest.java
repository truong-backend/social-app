package com.socialapp.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditMessageRequest(

        @NotBlank
        @Size(max = 10_000)
        String content
) {}