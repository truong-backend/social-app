package com.socialapp.application.usecase.chat;

import com.socialapp.domain.service.ChatDomainService;

public class EndCallUseCase {

    private final ChatDomainService chatDomainService;

    public EndCallUseCase(ChatDomainService chatDomainService) {
        this.chatDomainService = chatDomainService;
    }

    public void execute(String chatId, String callId) {
        chatDomainService.endCall(chatId, callId);
    }
}