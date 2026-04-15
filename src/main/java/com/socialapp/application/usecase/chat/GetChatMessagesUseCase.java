package com.socialapp.application.usecase.chat;

import com.socialapp.application.dto.response.MessageResponse;
import com.socialapp.application.mapper.MessageMapper;
import com.socialapp.domain.model.aggregate.Chat;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.ChatRepository;

import java.util.List;

public class GetChatMessagesUseCase {

    private final ChatRepository chatRepository;
    private final MessageMapper  messageMapper;

    public GetChatMessagesUseCase(ChatRepository chatRepository, MessageMapper messageMapper) {
        this.chatRepository = chatRepository;
        this.messageMapper  = messageMapper;
    }

    public List<MessageResponse> execute(String chatId, String requesterId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found: " + chatId));

        if (!chat.isMember(new UserId(requesterId)))
            throw new IllegalStateException("Sender is not a member of this chat");

        return chat.getMessages().stream()
                .map(messageMapper::toResponse)
                .toList();
    }
}