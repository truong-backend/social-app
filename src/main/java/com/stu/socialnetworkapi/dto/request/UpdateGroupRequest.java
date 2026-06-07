package com.stu.socialnetworkapi.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record UpdateGroupRequest(
        @NotNull UUID chatId,
        @Size(max = 100, message = "Group name is too long") String name,
        MultipartFile avatar
) {}