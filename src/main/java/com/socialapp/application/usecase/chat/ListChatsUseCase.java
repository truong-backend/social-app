package com.socialapp.application.usecase.chat;

import com.socialapp.application.dto.response.ChatResponse;
import com.socialapp.application.mapper.ChatMapper;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.ChatRepository;


import java.util.List;

public class ListChatsUseCase {

    private final ChatRepository chatRepository;
    private final ChatMapper     chatMapper;

    public ListChatsUseCase(ChatRepository chatRepository,
                            ChatMapper chatMapper) {
        this.chatRepository = chatRepository;
        this.chatMapper     = chatMapper;
    }

    public List<ChatResponse> execute(String userId) {
        return chatRepository.findByMemberId(new UserId(userId))
                .stream()
                .map(chatMapper::toResponse)
                .toList();
    }
}