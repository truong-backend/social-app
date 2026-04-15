package com.socialapp.application.usecase.chat;

import com.socialapp.application.dto.request.SendMessageRequest;
import com.socialapp.application.dto.response.MessageResponse;
import com.socialapp.application.mapper.MessageMapper;
import com.socialapp.domain.model.entity.Message;
import com.socialapp.domain.model.valueobject.MessageContent;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.ChatDomainService;

public class SendMessageUseCase {

    private final ChatDomainService chatDomainService;
    private final MessageMapper     messageMapper;

    public SendMessageUseCase(ChatDomainService chatDomainService,
                              MessageMapper messageMapper) {
        this.chatDomainService = chatDomainService;
        this.messageMapper     = messageMapper;
    }

    public MessageResponse execute(String userId, SendMessageRequest req) {
        Message message = chatDomainService.sendTextMessage(
                req.chatId(),
                new UserId(userId),
                new MessageContent(req.content() != null ? req.content() : "")
        );
        return messageMapper.toResponse(message);
    }
}