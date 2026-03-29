package com.socialapp.application.message.usecase;

import com.socialapp.application.message.dto.response.MessageResponseDtos;
import com.socialapp.domain.message.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class GetChatListUseCase {

    private final ChatRepository chatRepository;

    public GetChatListUseCase(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Transactional(readOnly = true)
    public List<MessageResponseDtos.ChatResponse> execute(String userId) {
        return chatRepository.findByUserId(userId)
                .stream()
                .map(c -> new MessageResponseDtos.ChatResponse(c.getId(), c.getMemberIds(), c.getCreatedAt()))
                .toList();
    }
}
