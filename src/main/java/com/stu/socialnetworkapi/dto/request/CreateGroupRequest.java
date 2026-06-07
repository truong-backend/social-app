package com.stu.socialnetworkapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateGroupRequest(
        @NotBlank(message = "Group name is required")
        @Size(max = 100, message = "Group name is too long")
        String name,

        @NotEmpty(message = "Members list cannot be empty")
        @Size(min = 2, max = 200, message = "Group must have 2-200 members")
        List<String> memberUsernames
) {}