package com.socialapp.application.usecase.chat;

import com.socialapp.domain.service.ChatDomainService;

public class MarkMessageReadUseCase {

    private final ChatDomainService chatDomainService;

    public MarkMessageReadUseCase(ChatDomainService chatDomainService) {
        this.chatDomainService = chatDomainService;
    }

    public void execute(String chatId, String messageId) {
        chatDomainService.markMessageRead(chatId, messageId);
    }
}