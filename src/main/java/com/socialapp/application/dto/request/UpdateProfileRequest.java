package com.socialapp.application.dto.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(

        String familyName,
        String givenName,

        @Past
        LocalDate birthdate,

        @Pattern(regexp = "^[a-zA-Z0-9._-]{1,32}$",
                message = "Username chỉ được chứa chữ cái, số, '.', '-', '_' và tối đa 32 ký tự")
        String username,

        @Size(max = 500)
        String bio
) {}