package com.socialapp.application.message.usecase;

import com.socialapp.application.message.dto.request.MessageRequestDtos.*;
import com.socialapp.application.message.dto.response.MessageResponseDtos.*;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.file.entity.FileNode;
import com.socialapp.domain.file.repository.FileRepository;
import com.socialapp.domain.message.entity.Chat;
import com.socialapp.domain.message.entity.Message;
import com.socialapp.domain.message.repository.ChatRepository;
import com.socialapp.domain.message.repository.MessageRepository;
import com.socialapp.domain.message.valueobject.DeleteType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

// ── SendMessageUseCase ───────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class SendMessageUseCase {

    private final ChatRepository    chatRepository;
    private final MessageRepository messageRepository;
    private final FileStorage       fileStorage;
    private final FileRepository    fileRepository;
    private final RealtimePublisher realtimePublisher;

    @Transactional
    public MessageResponse execute(String senderId, String targetId,
                                   SendMessageRequest request,
                                   List<MultipartFile> files) {

        // Lấy hoặc tạo chat giữa 2 người
        Chat chat = chatRepository.findDirectChatBetween(senderId, targetId)
                .orElseGet(() -> {
                    Chat newChat = Chat.createDirect(senderId, targetId);
                    return chatRepository.save(newChat);
                });

        chat.validateMember(senderId);

        List<String> paths = uploadFiles(files);
        Message message = Message.create(senderId, chat.getId(), request.content(), paths);
        messageRepository.save(message);

        // Push realtime đến tất cả thành viên
        realtimePublisher.publishToChat(chat.getId(), "NEW_MESSAGE", toResponse(message));

        return toResponse(message);
    }

    private List<String> uploadFiles(List<MultipartFile> files) {
        List<String> paths = new ArrayList<>();
        if (files == null) return paths;
        for (MultipartFile f : files) {
            String path = fileStorage.upload(f);
            fileRepository.save(FileNode.create(path, f.getOriginalFilename(), f.getContentType()));
            paths.add(path);
        }
        return paths;
    }

    private MessageResponse toResponse(Message m) {
        return new MessageResponse(m.getId(), m.getSenderId(), m.getChatId(),
                m.getContent(), m.getAttachedFilePaths(),
                m.isRead(), m.getSentAt(), m.getUpdatedAt());
    }
}

// ── UpdateMessageUseCase ─────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class UpdateMessageUseCase {

    private final MessageRepository messageRepository;
    private final ChatRepository    chatRepository;
    private final RealtimePublisher realtimePublisher;

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

@Service
@RequiredArgsConstructor
class DeleteMessageUseCase {

    private final MessageRepository messageRepository;
    private final FileStorage       fileStorage;
    private final FileRepository    fileRepository;
    private final RealtimePublisher realtimePublisher;

    @Transactional
    public SimpleMessageResponse execute(String requesterId, String messageId,
                                         DeleteMessageRequest request) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        DeleteType type = DeleteType.valueOf(request.type());

        if (type == DeleteType.EVERY) {
            message.deleteForEveryone(requesterId);
            fileStorage.deleteAll(message.getAttachedFilePaths());
            fileRepository.deleteByPaths(message.getAttachedFilePaths());
            realtimePublisher.publishToChat(message.getChatId(), "MESSAGE_DELETED", messageId);
        } else {
            message.deleteForSender(requesterId);
        }

        messageRepository.save(message);
        return new SimpleMessageResponse("Message deleted");
    }
}

// ── GetChatListUseCase ───────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class GetChatListUseCase {

    private final ChatRepository chatRepository;

    @Transactional(readOnly = true)
    public List<ChatResponse> execute(String userId) {
        return chatRepository.findByUserId(userId)
                .stream()
                .map(c -> new ChatResponse(c.getId(), c.getMemberIds(), c.getCreatedAt()))
                .toList();
    }
}

// ── GetChatUseCase ───────────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class GetChatUseCase {

    private final ChatRepository    chatRepository;
    private final MessageRepository messageRepository;

    @Transactional(readOnly = true)
    public List<MessageResponse> execute(String requesterId, String chatId,
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
                .map(m -> new MessageResponse(m.getId(), m.getSenderId(), m.getChatId(),
                        m.getContent(), m.getAttachedFilePaths(),
                        m.isRead(), m.getSentAt(), m.getUpdatedAt()))
                .toList();
    }
}

// ── SearchChatUseCase ────────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class SearchChatUseCase {

    private final ChatRepository chatRepository;

    @Transactional(readOnly = true)
    public List<ChatResponse> execute(String userId, String query) {
        return chatRepository.searchByUserId(query, userId)
                .stream()
                .map(c -> new ChatResponse(c.getId(), c.getMemberIds(), c.getCreatedAt()))
                .toList();
    }
}
