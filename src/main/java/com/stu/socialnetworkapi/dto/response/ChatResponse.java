package com.stu.socialnetworkapi.dto.response;

import com.stu.socialnetworkapi.enums.BlockStatus;
import com.stu.socialnetworkapi.enums.GroupRole;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ChatResponse {
    UUID chatId;
    String name;
    MessageResponse latestMessage;

    // Direct chat
    UserCommonInformationResponse target;
    int notReadMessageCount;
    BlockStatus blockStatus;

    // Group chat
    Boolean isGroup;
    String groupAvatarUrl;
    List<GroupMemberResponse> members;
    GroupRole myRole;
    int memberCount;
}