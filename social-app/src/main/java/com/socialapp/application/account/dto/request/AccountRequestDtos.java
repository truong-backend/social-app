package com.socialapp.application.account.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AccountRequestDtos {

    public record RegisterRequest(
            @NotBlank @Email
            String email,

            @NotBlank @Size(min = 8, max = 64)
            String password,

            @NotBlank
            String familyName,

            @NotBlank
            String givenName,

            @NotBlank
            String birthdate    // "yyyy-MM-dd"
    ) {}

    public record LoginRequest(
            @NotBlank @Email
            String email,

            @NotBlank
            String password
    ) {}

    public record ConfirmEmailRequest(
            @NotBlank
            String code
    ) {}

    public record PrepareResetPasswordRequest(
            @NotBlank @Email
            String email
    ) {}

    public record ConfirmResetCodeRequest(
            @NotBlank
            String code
    ) {}

    public record UpdatePasswordRequest(
            @NotBlank @Size(min = 8, max = 64)
            String newPassword,

            @NotBlank @Size(min = 8, max = 64)
            String confirmPassword
    ) {}
}