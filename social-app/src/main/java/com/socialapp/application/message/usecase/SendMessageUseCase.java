package com.socialapp.application.message.usecase;

import com.socialapp.application.message.dto.request.MessageRequestDtos;
import com.socialapp.application.message.dto.response.MessageResponseDtos;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.file.entity.FileNode;
import com.socialapp.domain.file.repository.FileRepository;
import com.socialapp.domain.message.entity.Chat;
import com.socialapp.domain.message.entity.Message;
import com.socialapp.domain.message.repository.ChatRepository;
import com.socialapp.domain.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SendMessageUseCase {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final FileStorage fileStorage;
    private final FileRepository fileRepository;
    private final RealtimePublisher realtimePublisher;

    @Transactional
    public MessageResponseDtos.MessageResponse execute(String senderId, String targetId,
                                                       MessageRequestDtos.SendMessageRequest request,
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

    private MessageResponseDtos.MessageResponse toResponse(Message m) {
        return new MessageResponseDtos.MessageResponse(m.getId(), m.getSenderId(), m.getChatId(),
                m.getContent(), m.getAttachedFilePaths(),
                m.isRead(), m.getSentAt(), m.getUpdatedAt());
    }
}
