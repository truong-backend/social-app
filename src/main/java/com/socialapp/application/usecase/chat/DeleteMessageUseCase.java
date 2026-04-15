package com.socialapp.application.usecase.chat;

import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.ChatDomainService;

public class DeleteMessageUseCase {

    private final ChatDomainService chatDomainService;

    public DeleteMessageUseCase(ChatDomainService chatDomainService) {
        this.chatDomainService = chatDomainService;
    }

    public void execute(String userId, String chatId, String messageId) {
        chatDomainService.deleteMessage(chatId, messageId, new UserId(userId));
    }
}