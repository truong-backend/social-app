package com.socialapp.application.message.usecase;

import com.socialapp.application.message.dto.request.MessageRequestDtos.UpdateMessageRequest;
import com.socialapp.application.message.dto.response.MessageResponseDtos.MessageResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.message.entity.Message;
import com.socialapp.domain.message.repository.ChatRepository;
import com.socialapp.domain.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ── SendMessageUseCase ───────────────────────────────────────────────────────

// ── UpdateMessageUseCase ─────────────────────────────────────────────────────


public class UpdateMessageUseCase {

    private final MessageRepository messageRepository;
    private final ChatRepository    chatRepository;
    private final RealtimePublisher realtimePublisher;

    public UpdateMessageUseCase(MessageRepository messageRepository, ChatRepository chatRepository, RealtimePublisher realtimePublisher) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.realtimePublisher = realtimePublisher;
    }

    @Transactional
    public MessageResponse execute(String requesterId, String messageId,
                                   UpdateMessageRequest request) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        // Domain enforce sender check
        message.updateContent(requesterId, request.content());
        messageRepository.save(message);

        realtimePublisher.publishToChat(message.getChatId(), "MESSAGE_UPDATED",
                toResponse(message));

        return toResponse(message);
    }

    private MessageResponse toResponse(Message m) {
        return new MessageResponse(m.getId(), m.getSenderId(), m.getChatId(),
                m.getContent(), m.getAttachedFilePaths(),
                m.isRead(), m.getSentAt(), m.getUpdatedAt());
    }
}

// ── DeleteMessageUseCase ─────────────────────────────────────────────────────

// ── GetChatListUseCase ───────────────────────────────────────────────────────

// ── GetChatUseCase ───────────────────────────────────────────────────────────

// ── SearchChatUseCase ────────────────────────────────────────────────────────

