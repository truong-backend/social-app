package com.socialapp.application.message.usecase;

import com.socialapp.domain.message.repository.ChatRepository;
import com.socialapp.domain.message.repository.MessageRepository;
import com.socialapp.application.shared.exception.ForbiddenException;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

public class MarkMessagesReadUseCase {

    private final ChatRepository    chatRepository;
    private final MessageRepository messageRepository;

    public MarkMessagesReadUseCase(ChatRepository chatRepository,
                                   MessageRepository messageRepository) {
        this.chatRepository    = chatRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public void execute(String userId, String chatId) {
        var chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        if (!chat.hasMember(userId)) {
            throw new ForbiddenException("Not a member of this chat");
        }
        messageRepository.markChatMessagesAsRead(chatId, userId);
    }
}