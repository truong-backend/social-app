package com.stu.socialnetworkapi.dto.projection;

import com.stu.socialnetworkapi.enums.BlockStatus;
import com.stu.socialnetworkapi.enums.GroupRole;
import com.stu.socialnetworkapi.enums.MessageType;

import java.time.ZonedDateTime;
import java.util.UUID;

public record ChatProjection(
        UUID chatId,
        String name,
        UUID latestMessageId,
        String latestMessageContent,
        String latestMessageFileId,
        ZonedDateTime latestMessageSentAt,
        UUID latestMessageSenderId,
        String latestMessageSenderUsername,
        String latestMessageSenderGivenName,
        String latestMessageSenderFamilyName,
        String latestMessageSenderProfilePictureId,
        Boolean latestMessageDeleted,
        MessageType latestMessageType,
        String latestMessageCallId,
        ZonedDateTime latestMessageCallAt,
        ZonedDateTime latestMessageEndAt,
        Boolean latestMessageAnswered,
        Boolean latestMessageIsVideoCall,

        // Direct chat fields
        UUID targetId,
        String targetUsername,
        String targetGivenName,
        String targetFamilyName,
        String targetProfilePictureId,
        int notReadMessageCount,
        boolean isFriend,
        BlockStatus blockStatus,

        // Group chat fields
        Boolean isGroup,
        String groupAvatarFileId,
        int memberCount,
        GroupRole myRole
) {}