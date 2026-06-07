package com.stu.socialnetworkapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record GroupMemberRequest(
        @NotNull UUID chatId,
        List<String> usernames,   // for add members
        String username            // for remove/promote single member
) {}