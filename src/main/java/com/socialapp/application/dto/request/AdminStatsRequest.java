package com.socialapp.application.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AdminStatsRequest(

        @NotNull
        LocalDate from,

        @NotNull
        LocalDate to
) {}