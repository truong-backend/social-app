package com.stu.socialnetworkapi.mapper;

import com.stu.socialnetworkapi.dto.projection.ChatProjection;
import com.stu.socialnetworkapi.dto.response.ChatResponse;
import com.stu.socialnetworkapi.enums.GroupRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMapper {
    private final UserMapper userMapper;
    private final MessageMapper messageMapper;

    public ChatResponse toChatResponse(final ChatProjection projection) {
        if (projection == null) return null;

        ChatResponse.ChatResponseBuilder builder = ChatResponse.builder()
                .chatId(projection.chatId())
                .name(projection.name())
                .latestMessage(messageMapper.toMessageResponse(projection))
                .notReadMessageCount(projection.notReadMessageCount())
                .isGroup(Boolean.TRUE.equals(projection.isGroup()));

        if (Boolean.TRUE.equals(projection.isGroup())) {
            // Group chat
            builder
                    .groupAvatarUrl(projection.groupAvatarFileId())
                    .memberCount(projection.memberCount())
                    .myRole(projection.myRole() != null
                            ? GroupRole.valueOf(projection.myRole().name())
                            : GroupRole.MEMBER)
                    .blockStatus(null);
        } else {
            // Direct chat
            builder
                    .target(userMapper.toTargetUserCommonInformationResponse(projection))
                    .blockStatus(projection.blockStatus());
        }

        return builder.build();
    }
}