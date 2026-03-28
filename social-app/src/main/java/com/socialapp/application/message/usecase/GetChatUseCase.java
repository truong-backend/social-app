package com.socialapp.application.message.usecase;

import com.socialapp.application.message.dto.response.MessageResponseDtos;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.message.entity.Chat;
import com.socialapp.domain.message.repository.ChatRepository;
import com.socialapp.domain.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetChatUseCase {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    @Transactional(readOnly = true)
    public List<MessageResponseDtos.MessageResponse> execute(String requesterId, String chatId,
                                                             int skip, int limit) {

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        chat.validateMember(requesterId);

        return messageRepository.findByChatId(chatId, skip, limit)
                .stream()
                .filter(m -> {
                    // Lọc message đã xóa với sender
                    if (m.getSenderId().equals(requesterId) && m.isDeletedForSender())
                        return false;
                    return !m.isDeletedForEveryone();
                })
                .map(m -> new MessageResponseDtos.MessageResponse(m.getId(), m.getSenderId(), m.getChatId(),
                        m.getContent(), m.getAttachedFilePaths(),
                        m.isRead(), m.getSentAt(), m.getUpdatedAt()))
                .toList();
    }
}
