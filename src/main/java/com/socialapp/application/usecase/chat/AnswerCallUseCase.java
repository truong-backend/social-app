package com.socialapp.application.usecase.chat;

import com.socialapp.domain.service.ChatDomainService;

public class AnswerCallUseCase {

    private final ChatDomainService chatDomainService;

    public AnswerCallUseCase(ChatDomainService chatDomainService) {
        this.chatDomainService = chatDomainService;
    }

    public void execute(String chatId, String callId) {
        chatDomainService.answerCall(chatId, callId);
    }
}