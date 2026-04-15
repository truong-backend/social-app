package com.socialapp.application.usecase.chat;

import com.socialapp.application.dto.response.ChatResponse;
import com.socialapp.application.mapper.ChatMapper;
import com.socialapp.domain.model.aggregate.Chat;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.ChatRepository;
import com.socialapp.domain.service.ChatDomainService;

public class GetOrCreateChatUseCase {

    private final ChatDomainService chatDomainService;
    private final ChatRepository    chatRepository;
    private final ChatMapper        chatMapper;

    public GetOrCreateChatUseCase(ChatDomainService chatDomainService,
                                  ChatRepository chatRepository,
                                  ChatMapper chatMapper) {
        this.chatDomainService = chatDomainService;
        this.chatRepository    = chatRepository;
        this.chatMapper        = chatMapper;
    }

    public ChatResponse execute(String userAId, String userBId) {
        UserId a = new UserId(userAId);
        UserId b = new UserId(userBId);

        Chat chat = chatRepository.findPrivateChat(a, b)
                .orElseGet(() -> chatDomainService.createChat(a, b));

        return chatMapper.toResponse(chat);
    }
}