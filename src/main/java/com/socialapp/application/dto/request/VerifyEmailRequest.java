package com.socialapp.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(

        @NotBlank
        String accountId,

        @NotBlank
        String code
) {}