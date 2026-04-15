package com.socialapp.application.usecase.chat;


import com.socialapp.application.dto.response.CallResponse;
import com.socialapp.domain.model.entity.Call;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.ChatDomainService;

public class StartCallUseCase {
    private final ChatDomainService chatDomainService;

    public StartCallUseCase(ChatDomainService chatDomainService) {
        this.chatDomainService = chatDomainService;
    }

    public CallResponse execute(String callerId, String chatId, boolean isVideoCall) {
        Call call = chatDomainService.startCall(chatId, new UserId(callerId), isVideoCall);
        return new CallResponse(
                call.getId(), call.getCallId(), callerId,
                isVideoCall, false, false,
                call.getCallAt(), null
        );
    }
}