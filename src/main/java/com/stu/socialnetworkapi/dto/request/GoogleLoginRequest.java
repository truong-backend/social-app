package com.stu.socialnetworkapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "GOOGLE_TOKEN_REQUIRED")
        String googleToken
) {}