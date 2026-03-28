package com.socialapp.application.message.usecase;

import com.socialapp.application.message.dto.request.MessageRequestDtos;
import com.socialapp.application.message.dto.response.MessageResponseDtos;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.file.repository.FileRepository;
import com.socialapp.domain.message.entity.Message;
import com.socialapp.domain.message.repository.MessageRepository;
import com.socialapp.domain.message.valueobject.DeleteType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteMessageUseCase {

    private final MessageRepository messageRepository;
    private final FileStorage fileStorage;
    private final FileRepository fileRepository;
    private final RealtimePublisher realtimePublisher;

    @Transactional
    public MessageResponseDtos.SimpleMessageResponse execute(String requesterId, String messageId,
                                                             MessageRequestDtos.DeleteMessageRequest request) {

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
        return new MessageResponseDtos.SimpleMessageResponse("Message deleted");
    }
}
