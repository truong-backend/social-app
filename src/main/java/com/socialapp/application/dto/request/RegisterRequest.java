package com.socialapp.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record RegisterRequest(

        @NotBlank @Email
        String email,

        @NotBlank
        String password,

        @NotBlank
        String familyName,

        @NotBlank
        String givenName,

        @NotNull @Past
        LocalDate birthdate
) {}