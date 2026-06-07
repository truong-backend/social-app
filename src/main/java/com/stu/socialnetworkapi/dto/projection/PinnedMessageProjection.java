package com.stu.socialnetworkapi.dto.projection;

import java.time.ZonedDateTime;
import java.util.UUID;

public record PinnedMessageProjection(
        UUID id,
        String content,
        ZonedDateTime sentAt,
        String senderUsername
) {}