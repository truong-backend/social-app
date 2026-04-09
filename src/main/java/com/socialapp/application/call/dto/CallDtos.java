package com.socialapp.application.call.dto;

import java.time.LocalDateTime;

public class CallDtos {

    public record InitiateCallRequest(boolean isVideoCall) {}

    public record CallResponse(
            String callId,
            String roomId,
            String chatId,
            String callerId,
            boolean isVideoCall,
            LocalDateTime initiatedAt
    ) {}

    public record EndCallRequest(String callId) {}
}