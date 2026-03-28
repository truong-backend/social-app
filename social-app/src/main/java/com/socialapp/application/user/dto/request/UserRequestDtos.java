package com.socialapp.application.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRequestDtos {

    public record ChangeNameRequest(
            @NotBlank String familyName,
            @NotBlank String givenName
    ) {}

    public record ChangeUsernameRequest(
            @NotBlank @Size(min = 3, max = 30) String username
    ) {}

    public record ChangeBirthdateRequest(
            @NotBlank String birthdate   // "yyyy-MM-dd"
    ) {}

    public record ChangeBioRequest(
            @Size(max = 500) String bio
    ) {}
}