package com.socialapp.application.mapper;

import com.socialapp.application.dto.response.ChatResponse;
import com.socialapp.application.dto.response.MessageResponse;
import com.socialapp.domain.model.aggregate.Chat;
import com.socialapp.domain.model.entity.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatMapper {

    private final MessageMapper messageMapper;

    public ChatMapper(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    public ChatResponse toResponse(Chat chat) {
        List<String> memberIds = chat.getMembers().stream()
                .map(uid -> uid.getValue())
                .toList();

        List<Message> msgs = chat.getMessages();
        MessageResponse lastMessage = msgs.isEmpty()
                ? null
                : messageMapper.toResponse(msgs.get(msgs.size() - 1));

        return new ChatResponse(
                chat.getId(),
                memberIds,
                lastMessage,
                chat.getCreatedAt()
        );
    }
}