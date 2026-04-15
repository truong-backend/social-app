package com.socialapp.application.mapper;

import com.socialapp.application.dto.response.MessageResponse;
import com.socialapp.domain.model.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageResponse toResponse(Message message) {
        return toResponse(message, null);
    }

    public MessageResponse toResponse(Message message, String senderName) {
        String attachmentUrl = (message.getAttachedFile() != null)
                ? message.getAttachedFile().getMeta().getPath()
                : null;

        String content = message.isDeleted() ? null : message.getContent().getValue();

        return new MessageResponse(
                message.getId(),
                message.getSenderId().getValue(),
                senderName,
                content,
                message.isRead(),
                message.isDeleted(),
                attachmentUrl,
                message.getSentAt(),
                message.getUpdatedAt()
        );
    }
}