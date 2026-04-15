package com.socialapp.application.usecase.chat;

import com.socialapp.application.dto.request.EditMessageRequest;
import com.socialapp.application.dto.response.MessageResponse;
import com.socialapp.application.mapper.MessageMapper;
import com.socialapp.domain.model.aggregate.Chat;
import com.socialapp.domain.model.entity.Message;
import com.socialapp.domain.model.valueobject.MessageContent;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.ChatRepository;
import com.socialapp.domain.service.ChatDomainService;

public class EditMessageUseCase {

    private final ChatDomainService chatDomainService;
    private final ChatRepository    chatRepository;
    private final MessageMapper     messageMapper;

    public EditMessageUseCase(ChatDomainService chatDomainService,
                              ChatRepository chatRepository,
                              MessageMapper messageMapper) {
        this.chatDomainService = chatDomainService;
        this.chatRepository    = chatRepository;
        this.messageMapper     = messageMapper;
    }

    public MessageResponse execute(String userId, String chatId,
                                   String messageId, EditMessageRequest req) {
        chatDomainService.editMessage(
                chatId,
                messageId,
                new UserId(userId),
                new MessageContent(req.content())
        );
        // Reload để trả về state mới nhất
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        Message updated = chat.findMessageById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

        return messageMapper.toResponse(updated);
    }
}